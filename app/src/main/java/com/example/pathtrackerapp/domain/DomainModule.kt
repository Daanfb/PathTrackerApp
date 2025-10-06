package com.example.pathtrackerapp.domain

import com.example.pathtrackerapp.data.repository.LocationRepositoryImpl
import com.example.pathtrackerapp.data.permissions.PermissionManager
import com.example.pathtrackerapp.data.repository.TrackingSessionRepositoryImpl
import com.example.pathtrackerapp.domain.permissions.PermissionHandler
import com.example.pathtrackerapp.domain.repository.LocationRepository
import com.example.pathtrackerapp.domain.repository.TrackingSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindPermissionHandler(permissionManager: PermissionManager): PermissionHandler

    @Binds
    @Singleton
    abstract fun bindLocationRepository(locationRepositoryImpl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindTrackingSessionRepository(trackingSessionRepositoryImpl: TrackingSessionRepositoryImpl): TrackingSessionRepository
}