package com.example.pathtrackerapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionPoint(
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long
)