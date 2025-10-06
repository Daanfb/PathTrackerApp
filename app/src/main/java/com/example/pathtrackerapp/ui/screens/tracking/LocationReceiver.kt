package com.example.pathtrackerapp.ui.screens.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.example.pathtrackerapp.data.repository.LocationRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var locationRepository: LocationRepositoryImpl

    override fun onReceive(context: Context, intent: Intent) {

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        locationRepository.updateLocationStatus(isGpsEnabled || isNetworkEnabled)
    }
}
