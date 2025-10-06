package com.example.pathtrackerapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class MainDestinations{

    @Serializable
    data object Tracking: MainDestinations()

    @Serializable
    data object SessionLogs: MainDestinations()
}

@Serializable
data class TrackingSessionSummary(val summarySession: String)