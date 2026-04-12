package com.biprangshu.guardiansathi.Elder.data

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.jvm.java

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

    override fun isBatteryOptimizationIgnored(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(context.packageName) == true
    }
}