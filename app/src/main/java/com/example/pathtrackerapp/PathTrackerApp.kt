package com.example.pathtrackerapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.pathtrackerapp.service.TrackingForegroundService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PathTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Creates a notification channel for the tracking foreground service.
     * This is required for Android O and above to display notifications.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TrackingForegroundService.NOTIFICATION_CHANNEL_ID,
            "Tracking Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}