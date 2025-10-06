package com.example.pathtrackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a location point associated with a tracking session.
 * Each location point includes latitude, longitude, and a timestamp.
 * The sessionId field is a foreign key referencing the SessionEntity.
 */
@Entity(
    tableName = "location_points",
    indices = [Index("sessionId")],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
