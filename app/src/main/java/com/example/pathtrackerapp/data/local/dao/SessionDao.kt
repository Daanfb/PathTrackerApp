package com.example.pathtrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.pathtrackerapp.data.local.entity.LocationPointEntity
import com.example.pathtrackerapp.data.local.entity.SessionEntity
import com.example.pathtrackerapp.data.local.relation.SessionWithPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Insert
    suspend fun insertPoints(points: List<LocationPointEntity>)

    @Transaction
    suspend fun insertSessionWithPoints(session: SessionEntity, points: List<LocationPointEntity>) {
        insertSession(session)
        insertPoints(points)
    }

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startTimeMillis DESC")
    fun observeAllSessionsWithPointsFlow(): Flow<List<SessionWithPoints>>
}