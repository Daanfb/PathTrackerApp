package com.example.pathtrackerapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.pathtrackerapp.MainActivity
import com.example.pathtrackerapp.R
import com.example.pathtrackerapp.data.repository.TrackingRepository
import com.example.pathtrackerapp.domain.model.SessionPoint
import com.example.pathtrackerapp.domain.model.TrackingSession
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class TrackingForegroundService @Inject constructor() : Service(), SensorEventListener {

    @Inject
    lateinit var trackingRepository: TrackingRepository

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var sensorManager: SensorManager

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Coroutine scope for background tasks
    // SupervisorJob to ensure one failing child doesn't cancel the whole scope
    // Default dispatcher because it is not UI related work
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var startMillis: Long = 0L
    private var lastLocation: Location? = null

    private var stepCounterSensor: Sensor? = null
    private var initialSteps: Int = -1

    override fun onCreate() {
        super.onCreate()

        buildLocationRequest()
        buildLocationCallback()

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()

        // In case service is destroyed, ensure tracking is stopped
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

        // Stop the service in the repository state
        trackingRepository.stopTracking()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTrackingAndNavToSummary()
        }

        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startTracking() {
        startMillis = System.currentTimeMillis()

        // Set the service as running in the repository
        trackingRepository.startTracking()

        val initialNotification = buildNotification()
        startForeground(NOTIFICATION_ID, initialNotification)// Start service as foreground

        // Start location listener
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Start step counter listener
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start a coroutine to update the tracking state
        serviceScope.launch {
            while (isActive) {

                val elapsedTimeSeconds = (System.currentTimeMillis() - startMillis) / 1000

                trackingRepository.updateTrackingState {
                    it.copy(elapsedTimeSeconds = elapsedTimeSeconds)
                }

                delay(1000)
            }
        }
    }

    /**
     * Stops tracking, cleans up resources, and navigates to the summary screen.
     * The summary data is passed via an Intent extra in JSON format.
     */
    private fun stopTrackingAndNavToSummary() {

        // Cancel the tracking coroutine and listeners
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

        val finalTrackingState = trackingRepository.trackingState.value

        // Set the service as not running in the repository
        trackingRepository.stopTracking()

        val elapsedTimeSeconds = finalTrackingState.elapsedTimeSeconds
        val totalDistanceMeters = finalTrackingState.distanceMeters

        val averageSpeedKmh = if (elapsedTimeSeconds > 0)
            (totalDistanceMeters / 1000.0) / (elapsedTimeSeconds.toDouble() / 3600.0)
        else 0.0

        val summary = TrackingSession(
            id = "", // ID will be generated by the database
            title = "", // Title will be set by the user later
            steps = finalTrackingState.steps,
            distanceMeters = totalDistanceMeters,
            averageSpeedKmh = averageSpeedKmh,
            durationSeconds = elapsedTimeSeconds,
            startTimeMillis = startMillis,
            points = finalTrackingState.pathPoints
        )

        val encodedSummary = Json.encodeToString(TrackingSession.serializer(), summary)

        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SHOW_SUMMARY
            putExtra(EXTRA_TRACKING_SUMMARY, encodedSummary)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)

        // Stop foreground service and remove notification
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(1f)
            .setMinUpdateIntervalMillis(1000L)
            .build()
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    handleLocationPoint(location)
                }
            }
        }
    }

    /**
     * Processes a new location point, updating distance, speed, and path points.
     * Filters out inaccurate locations and large jumps.
     * Also updates the tracking state with the new data.
     */
    private fun handleLocationPoint(loc: Location) {

        // Ignore inaccurate locations
        // If accuracy is worse than 80 meters, discard the point
        if (loc.hasAccuracy() && loc.accuracy > 80f) return

        // Compute speed (km/h). Clamp to 0 if speed is negative or unavailable
        val speedKmh = if (loc.hasSpeed() && loc.speed >= 0f) loc.speed * 3.6f else 0f

        // Compute distance from last location, if available
        val distanceFromLast = lastLocation?.distanceTo(loc)?.toDouble() ?: 0.0
        val shouldAddDistance = distanceFromLast < 1000.0  // Filter out large jumps

        // Update tracking state with new distance and speed
        trackingRepository.updateTrackingState {
            val newDistance =
                if (shouldAddDistance) it.distanceMeters + distanceFromLast else it.distanceMeters
            it.copy(
                distanceMeters = newDistance,
                currentSpeedKmh = speedKmh
            )
        }

        // Update last location to current
        lastLocation = loc

        val locationPoint = SessionPoint(
            latitude = loc.latitude,
            longitude = loc.longitude,
            timestampMillis = loc.time
        )

        // Add point if it is sufficiently far from the last point
        if (shouldKeepPoint(locationPoint)) {
            trackingRepository.updateTrackingState {
                it.copy(pathPoints = it.pathPoints + locationPoint)
            }
        }
    }

    /**
     * Decides whether to keep the new location point based on distance from the last point.
     * If the distance is less than 3 meters, the point is discarded to reduce noise.
     */
    private fun shouldKeepPoint(nextLocationPoint: SessionPoint): Boolean {

        val pointsList = trackingRepository.trackingState.value.pathPoints

        if (pointsList.isEmpty()) return true
        val last = pointsList.last()

        // Calculate distance between last and next point
        val results = FloatArray(1)   // Needed for keep distanceBetween result
        Location.distanceBetween(
            last.latitude,
            last.longitude,
            nextLocationPoint.latitude,
            nextLocationPoint.longitude,
            results
        )

        return results[0] >= 3f  // Keep point if distance is greater than or equal to 3 meters
    }

    private fun buildNotification(): Notification {

        // Intent to stop the service when notification action is clicked
        val stopIntent = Intent(this, TrackingForegroundService::class.java).apply {
            action = ACTION_STOP
        }

        // PendingIntent for the stop action
        val stopPending =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Path Tracker")
            .setContentText("Tracking in progress")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis() - trackingRepository.trackingState.value.elapsedTimeSeconds * 1000) // Sincroniza el cronómetro
            .addAction(R.drawable.ic_stop, "Stop", stopPending)
            .build()
    }

    // Not used
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}


    /**
     * Handles step counter sensor events to update the step count in the tracking state.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { it ->
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val steps = it.values[0].toInt()

                // If initialSteps is -1, this is the first reading
                // We store this value to calculate steps relative to this initial value
                // as the step counter is cumulative since last device reboot
                if (initialSteps == -1) initialSteps = steps

                val currentSteps = steps - initialSteps

                // Update the tracking state with the new step count
                trackingRepository.updateTrackingState {
                    it.copy(steps = currentSteps)
                }
            }
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "path_tracker_channel"
        const val ACTION_START = "action_start_tracking"
        const val ACTION_STOP = "action_stop_tracking"
        const val ACTION_SHOW_SUMMARY = "action_show_summary"
        const val EXTRA_TRACKING_SUMMARY = "extra_tracking_summary"
        const val NOTIFICATION_ID = 4242
    }
}