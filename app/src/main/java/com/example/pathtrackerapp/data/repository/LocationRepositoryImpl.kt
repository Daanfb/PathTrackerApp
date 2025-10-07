package com.example.pathtrackerapp.data.repository

import android.content.Context
import android.location.LocationManager
import com.example.pathtrackerapp.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val _isLocationActivated = MutableStateFlow(false)

    override fun getLocationSettingsStatus(checkInitialStatus: Boolean): Flow<Boolean> {

        if(checkInitialStatus) {
            checkInitialLocationStatus()
        }

        return _isLocationActivated.asStateFlow()
    }

    override fun checkInitialLocationStatus(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val res = isGpsEnabled || isNetworkEnabled

        updateLocationStatus(res)

        return res
    }

    fun updateLocationStatus(isActivated: Boolean) {
        _isLocationActivated.update { isActivated }
    }
}