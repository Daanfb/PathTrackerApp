package com.example.pathtrackerapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pathtrackerapp.data.local.dao.SessionDao
import com.example.pathtrackerapp.data.local.entity.LocationPointEntity
import com.example.pathtrackerapp.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, LocationPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getSessionDao(): SessionDao
}