package com.example.pathtrackerapp.domain.repository

import com.example.pathtrackerapp.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow

interface TrackingSessionRepository {

    /**
     * Observe the flow of all tracking sessions.
     *
     * @return A Flow that emits a list of all TrackingSession objects.
     */
    fun observeAllSessionsFlow(): Flow<List<TrackingSession>>

    /**
     * Insert a new tracking session into the repository.
     *
     * @param session The TrackingSession object to be inserted.
     */
    suspend fun insertSession(session: TrackingSession)
}