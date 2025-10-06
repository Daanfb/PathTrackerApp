package com.example.pathtrackerapp.data.repository

import com.example.pathtrackerapp.data.local.dao.SessionDao
import com.example.pathtrackerapp.data.local.entity.LocationPointEntity
import com.example.pathtrackerapp.data.local.entity.SessionEntity
import com.example.pathtrackerapp.domain.model.TrackingSession
import com.example.pathtrackerapp.domain.repository.TrackingSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class TrackingSessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : TrackingSessionRepository {

    override fun observeAllSessionsFlow(): Flow<List<TrackingSession>> {
        return sessionDao.observeAllSessionsWithPointsFlow()
            .map { sessionEntities -> sessionEntities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun insertSession(session: TrackingSession) {

        val sessionId = UUID.randomUUID().toString()

        val sessionEntity = SessionEntity(
            id = sessionId,
            title = session.title,
            startTimeMillis = session.startTimeMillis,
            durationSeconds = session.durationSeconds,
            distanceMeters = session.distanceMeters,
            averageSpeedKmh = session.averageSpeedKmh,
            steps = session.steps,
        )

        val pointEntities = session.points.map { point ->
            LocationPointEntity(
                sessionId = sessionId,
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestampMillis
            )
        }

        sessionDao.insertSessionWithPoints(session = sessionEntity, points = pointEntities)
    }
}