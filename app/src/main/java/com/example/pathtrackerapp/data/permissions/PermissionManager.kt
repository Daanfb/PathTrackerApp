package com.example.pathtrackerapp.data.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.pathtrackerapp.domain.permissions.AppPermission
import com.example.pathtrackerapp.domain.permissions.PermissionHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionManager @Inject constructor(@ApplicationContext private val context: Context) :
    PermissionHandler {

    /**
     * Convert AppPermission to Android permission string
     */
    private fun toAndroidPermissions(permission: AppPermission): String {
        return when (permission) {
            AppPermission.LOCATION_FOREGROUND -> Manifest.permission.ACCESS_FINE_LOCATION
            AppPermission.LOCATION_BACKGROUND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                } else {
                    "" // No background location permission needed for versions below Q
                }
            }

            AppPermission.POST_NOTIFICATIONS -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                }else {
                    "" // No notification permission needed for versions below TIRAMISU
                }
            }
        }
    }

    /**
     * Check if the given permission is required for the current OS version
     */
    private fun isPermissionRequired(permission: AppPermission): Boolean {
        return toAndroidPermissions(permission).isNotBlank()
    }

    override fun isPermissionGranted(permission: AppPermission): Boolean {
        if (!isPermissionRequired(permission)) return true

        val permissionString = toAndroidPermissions(permission)


        val res = ContextCompat.checkSelfPermission(
            context,
            permissionString
        ) == PackageManager.PERMISSION_GRANTED

        return res
    }

    override fun askPermission(
        launcher: ActivityResultLauncher<String>,
        permission: AppPermission
    ) {
        if (!isPermissionRequired(permission)) return

        val permissionString = toAndroidPermissions(permission)
        launcher.launch(permissionString)
    }
}