package com.example.pathtrackerapp.ui.screens.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pathtrackerapp.data.repository.TrackingRepository
import com.example.pathtrackerapp.domain.repository.LocationRepository
import com.example.pathtrackerapp.domain.permissions.AppPermission
import com.example.pathtrackerapp.domain.permissions.PermissionHandler
import com.example.pathtrackerapp.service.TrackingForegroundService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class TrackingUiState(
    val hasLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isLocationActivated: Boolean = false,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionHandler: PermissionHandler,
    private val locationRepository: LocationRepository,
    trackingRepository: TrackingRepository
) : ViewModel() {

    val trackingState = trackingRepository.trackingState
    val isServiceTrackingRunning = trackingRepository.isServiceTrackingRunning

    private val _permissionState = MutableStateFlow(
        Pair(
            permissionHandler.isPermissionGranted(AppPermission.LOCATION_FOREGROUND),
            permissionHandler.isPermissionGranted(AppPermission.POST_NOTIFICATIONS)
        )
    )

    val uiState = combine(
        _permissionState,
        locationRepository.getLocationSettingsStatus(checkInitialStatus = true)
    ) { permissions, isLocationActivated ->
        TrackingUiState(
            hasLocationPermission = permissions.first,
            hasNotificationPermission = permissions.second,
            isLocationActivated = isLocationActivated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrackingUiState()
    )

    private val _cameraUpdate = MutableSharedFlow<CameraUpdate>()
    val cameraUpdate = _cameraUpdate.asSharedFlow()

    // SharedFlow to emit one-time events for location resolution
    private val _locationResolution = MutableSharedFlow<IntentSender?>()
    val locationResolution = _locationResolution.asSharedFlow()

    fun askForLocationPermission(launcher: ActivityResultLauncher<String>) {
        permissionHandler.askPermission(launcher, AppPermission.LOCATION_FOREGROUND)
    }

    fun askForNotificationPermission(launcher: ActivityResultLauncher<String>) {
        permissionHandler.askPermission(launcher, AppPermission.POST_NOTIFICATIONS)
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _permissionState.update {
            it.copy(first = isGranted)
        }

        // If permission is granted, check the initial location status
        if (isGranted) {
            locationRepository.checkInitialLocationStatus()
        }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        _permissionState.update {
            it.copy(second = isGranted)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun onCenterMapClick(){
        viewModelScope.launch {
            val latLng = getLastKnownLocation()
            latLng?.let {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 18f)
                _cameraUpdate.emit(cameraUpdate)
            }
        }
    }

    /**
     *
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getLastKnownLocation(): LatLng? {

        // If the location permission is not granted, return null
        if(!permissionHandler.isPermissionGranted(AppPermission.LOCATION_FOREGROUND)){
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()

        return location?.let {
            LatLng(location.latitude, location.longitude)
        }
    }

    /**
     * Check if location services are enabled and start the tracking service if they are. If not,
     * request the user to enable them.
     */
    fun checkLocationAndStartService() {
//        if (!isLocationActivated.value) {
        if (!uiState.value.isLocationActivated) {
            requestToEnableLocation()
        } else {
            startTrackingService()
        }
    }

    fun stopService() {
        stopTrackingService()
    }

    /**
     * Request the system to show a dialog to enable location services.
     * This function is called when the user click on the "LocationDisabled" banner
     */
    fun requestToEnableLocation() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val task = settingsClient.checkLocationSettings(builder.build())

        // If the location settings are not satisfied, this exception is thrown
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                val intentSender = exception.resolution.intentSender

                // Emit the IntentSender through the SharedFlow to be showed in the UI
                viewModelScope.launch {
                    _locationResolution.emit(intentSender)
                }
            }
        }
    }

    /**
     * Start the tracking service in the foreground
     */
    private fun startTrackingService() {
        val intent = Intent(context, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_START
        }

        context.startForegroundService(intent)
    }

    /**
     * Stop the tracking service and unbind it
     */
    private fun stopTrackingService() {
        val intent = Intent(context, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_STOP
        }

        context.startService(intent)
    }
}

