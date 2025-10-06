package com.example.pathtrackerapp.domain.permissions

import androidx.activity.result.ActivityResultLauncher

interface PermissionHandler {

    /**
     * Check if the given permission is granted
     *
     * @param permission The permission to check
     * @return True if the permission is granted, false otherwise
     */
    fun isPermissionGranted(permission: AppPermission): Boolean

    /**
     * Launch a dialog to ask for the given permission
     *
     * @param permission The permission to ask for
     */
    fun askPermission(launcher: ActivityResultLauncher<String>, permission: AppPermission)
}