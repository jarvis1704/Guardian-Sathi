package com.biprangshu.guardiansathi.Elder.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionManagerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): PermissionManagerRepository {
    override fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun areAllPermissionsGranted(): Boolean {
        val locationGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

        val notificationGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

        val backGroundLocationGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }

        return locationGranted && notificationGranted && backGroundLocationGranted
    }
}