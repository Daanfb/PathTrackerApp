package com.example.pathtrackerapp.service

import android.app.NotificationManager
import android.content.Context
import android.hardware.SensorManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @ServiceScoped
    fun provideSensorManager(
        @ApplicationContext context: Context
    ) = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ) = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}