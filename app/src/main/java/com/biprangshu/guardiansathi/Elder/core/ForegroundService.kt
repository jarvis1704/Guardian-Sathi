package com.biprangshu.guardiansathi.Elder.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.biprangshu.guardiansathi.Global.MainActivity
import com.biprangshu.guardiansathi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuardianService : Service() {  // Changed this line

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            // Fall Detection
            // accelerometerManager.detectFalls().collect { fallDetected ->
            //     if (fallDetected) {
            //         handleFallDetection()
            //     }
            // }
        }

        serviceScope.launch {
            // Location updates every 15 minutes
            while (isActive) {
                // sendLocationToGuardians()
                delay(15 * 60 * 1000) // 15 minutes
            }
        }

        serviceScope.launch {
            // Battery updates every 30 minutes
            while (isActive) {
                // sendBatteryStatus()
                delay(30 * 60 * 1000) // 30 minutes
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "GUARDIAN_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Guardian Saathi Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Guardian Saathi Active")
            .setContentText("Protecting your loved one")
            .setSmallIcon(R.drawable.ic_guardian)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}