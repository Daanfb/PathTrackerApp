package com.example.pathtrackerapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackingSession(
    val id: String,
    val title: String,
    val startTimeMillis: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val averageSpeedKmh: Double,
    val steps: Int,
    val points: List<SessionPoint>
)
