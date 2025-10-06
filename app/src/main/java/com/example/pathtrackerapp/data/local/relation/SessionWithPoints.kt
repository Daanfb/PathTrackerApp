package com.example.pathtrackerapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pathtrackerapp.data.local.entity.LocationPointEntity
import com.example.pathtrackerapp.data.local.entity.SessionEntity
import com.example.pathtrackerapp.domain.model.SessionPoint
import com.example.pathtrackerapp.domain.model.TrackingSession

/**
 * Data class that represents a tracking session along with its associated location points.
 */
data class SessionWithPoints(
    @Embedded
    val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val points: List<LocationPointEntity>
){
    fun toDomain(): TrackingSession{
        return TrackingSession(
            id = session.id,
            title = session.title,
            startTimeMillis = session.startTimeMillis,
            durationSeconds = session.durationSeconds,
            distanceMeters = session.distanceMeters,
            averageSpeedKmh = session.averageSpeedKmh,
            steps = session.steps,
            points = points.map { point ->
                SessionPoint(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    timestampMillis = point.timestamp
                )
            }
        )
    }
}