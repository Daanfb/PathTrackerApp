package com.example.pathtrackerapp.data.repository

import com.example.pathtrackerapp.domain.model.SessionPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// Data class para el estado
data class TrackingState(
    val elapsedTimeSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Float = 0f,
    val steps: Int = 0,
    val pathPoints: List<SessionPoint> = emptyList()
)

@Singleton
class TrackingRepository @Inject constructor() {

    // Indicate if the tracking service is running
    private val _isServiceTrackingRunning = MutableStateFlow(false)
    val isServiceTrackingRunning = _isServiceTrackingRunning.asStateFlow()

    // Tracking state flow
    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()

    fun startTracking() {
        // When start, reset the state and set service running to true
        _trackingState.update { TrackingState() }
        _isServiceTrackingRunning.update { true }
    }

    fun stopTracking() {
        // When stop, set service running to false
        _isServiceTrackingRunning.update { false }
    }

    fun updateTrackingState(updateAction: (TrackingState) -> TrackingState) {
        if (_isServiceTrackingRunning.value) {
            _trackingState.update(updateAction)
        }
    }
}