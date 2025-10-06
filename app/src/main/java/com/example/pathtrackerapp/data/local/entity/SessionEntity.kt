package com.example.pathtrackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity class representing a tracking session.
 * Each session includes details such as title, start time, duration, distance, average speed, and steps taken.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTimeMillis: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val averageSpeedKmh: Double,
    val steps: Int
)