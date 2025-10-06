package com.example.pathtrackerapp.ui.screens.tracking

import android.content.IntentFilter
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.pathtrackerapp.R
import com.example.pathtrackerapp.ui.utils.formatToTimeString
import com.google.android.gms.maps.model.JointType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline

@Composable
fun TrackingScreen(modifier: Modifier = Modifier, viewModel: TrackingViewModel = hiltViewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trackingState by viewModel.trackingState.collectAsStateWithLifecycle()
    val isServiceTrackingRunning by viewModel.isServiceTrackingRunning.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val animatedContainerColorButton by animateColorAsState(
        targetValue = if (isServiceTrackingRunning) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary
    )
    val animatedContentColorButton by animateColorAsState(
        targetValue = if (isServiceTrackingRunning) MaterialTheme.colorScheme.onError
        else MaterialTheme.colorScheme.onPrimary
    )

    val permissionLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onLocationPermissionResult(isGranted)
    }

    val permissionNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onNotificationPermissionResult(isGranted)
    }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {}
    )

    // Initial camera position (Madrid)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4168, -3.7038), 10f)
    }

    // If notification permission is not granted, ask for it when the composable is first launched
    LaunchedEffect(Unit) {
        if (!uiState.hasNotificationPermission) {
            viewModel.askForNotificationPermission(launcher = permissionNotificationLauncher)
        }
    }

    // Handle location resolution requests
    LaunchedEffect(Unit) {
        viewModel.locationResolution.collect { intentSender ->
            intentSender?.let {
                val req = IntentSenderRequest.Builder(it).build()
                intentSenderLauncher.launch(req)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cameraUpdate.collect { cameraUpdate ->
            cameraPositionState.animate(
                update = cameraUpdate, durationMs = 1000
            )
        }
    }

    // Register the LocationReceiver to listen for location provider changes
    DisposableEffect(Unit) {
        val receiver = LocationReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                properties = MapProperties(
                    isMyLocationEnabled = uiState.hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                ),
                cameraPositionState = cameraPositionState
            ) {

                // Remember the polyline points to avoid unnecessary recompositions
                val polylinePoints = remember(trackingState.pathPoints){
                    trackingState.pathPoints.map { LatLng(it.latitude, it.longitude) }
                }

                if (trackingState.pathPoints.size > 1) {
                    Polyline(
                        points = polylinePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 24f,
                        geodesic = true,
                        jointType = JointType.ROUND
                    )
                }
            }

            // Ask for location permission if not granted
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.hasLocationPermission,
                enter = slideInVertically { it },
                exit = slideOutVertically { -it },
            ) {
                RequestLocation(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp),
                    onButtonClick = {
                        if (!uiState.hasLocationPermission) {
                            viewModel.askForLocationPermission(permissionLocationLauncher)
                        }
                    })
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.isLocationActivated && uiState.hasLocationPermission,
                enter = slideInVertically { it },
                exit = slideOutVertically { -it },
            ) {
                LocationDisableBanner(onBannerClick = viewModel::requestToEnableLocation)
            }

            SmallFloatingActionButton(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 8.dp, end = 8.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onCenterMapClick
            ) {
                Icon(painter = painterResource(R.drawable.ic_my_location), contentDescription = stringResource(R.string.content_description_go_to_my_location))
            }

            Button(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                enabled = uiState.hasLocationPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedContainerColorButton,
                    contentColor = animatedContentColorButton
                ),
                onClick = {
                    if (!isServiceTrackingRunning) {
                        viewModel.checkLocationAndStartService()
                    } else {
                        viewModel.stopService()
                    }
                }) {
                Text(stringResource(if (isServiceTrackingRunning) R.string.stop else R.string.start))
            }
        }

        AnimatedVisibility(
            visible = isServiceTrackingRunning
        ) {
            Stats(
                timeSeconds = trackingState.elapsedTimeSeconds,
                distanceMeters = trackingState.distanceMeters,
                speedKmh = trackingState.currentSpeedKmh,
                steps = trackingState.steps
            )
        }
    }
}

@Composable
private fun RequestLocation(modifier: Modifier = Modifier, onButtonClick: () -> Unit) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = stringResource(R.string.required_location),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.required_location_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(modifier = Modifier.align(Alignment.End), onClick = onButtonClick) {
            Text(text = stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun LocationDisableBanner(onBannerClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .clickable(onClick = onBannerClick)
    ) {
        Text(
            text = stringResource(R.string.no_gps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onError,
            modifier = Modifier.statusBarsPadding().padding(16.dp)
        )
    }
}

@Composable
private fun Stats(
    timeSeconds: Long,
    distanceMeters: Double,
    speedKmh: Float,
    steps: Int
) {

    val distanceText = if (distanceMeters >= 1000) {
        "${"%.2f".format(distanceMeters / 1000)} km"
    } else {
        "${distanceMeters.toInt()} m"
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StatColumn(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.duration),
                value = formatToTimeString(seconds = timeSeconds)
            )
            StatColumn(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.distance),
                value = distanceText
            )
            StatColumn(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.speed),
                value = "${"%.2f".format(speedKmh)} km/h"
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.steps),
                value = steps.toString()
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun StatColumn(modifier: Modifier = Modifier, title: String, value: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}