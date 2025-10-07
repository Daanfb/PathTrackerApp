package com.example.pathtrackerapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    /**
     * Emits the status of location settings.
     *
     * @return A Flow that emits true if location settings are satisfied, false otherwise.
     */
    fun getLocationSettingsStatus(checkInitialStatus: Boolean = false): Flow<Boolean>

    /**
     * Checks the initial status of location settings.
     */
    fun checkInitialLocationStatus(): Boolean
}
