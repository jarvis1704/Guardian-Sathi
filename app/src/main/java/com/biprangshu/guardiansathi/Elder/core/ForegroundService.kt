package com.biprangshu.guardiansathi.Elder.core

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.biprangshu.guardiansathi.Global.MainActivity
import com.biprangshu.guardiansathi.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java


@AndroidEntryPoint
class GuardianService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "GUARDIAN_SERVICE_CHANNEL"

        fun start(context: Context) {
            val intent = Intent(context, GuardianService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardianService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    // KEY FIX 1: START_STICKY makes Android restart the service if killed
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("GuardianService", "✅ Service started — monitoring is active")
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        // Cancel any previous loops before starting new ones (safe on restart)
        serviceScope.coroutineContext.cancelChildren()

        serviceScope.launch {
            while (isActive) {
                // TODO: sendLocationToGuardians()
                delay(15 * 60 * 1000L)
            }
        }

        serviceScope.launch {
            while (isActive) {
                // TODO: sendBatteryStatus()
                delay(30 * 60 * 1000L)
            }
        }

        // Fall detection will go here (sensor-based, no loop needed)
        // startFallDetection()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Guardian Saathi Protection",
            NotificationManager.IMPORTANCE_LOW  // Low = no sound, but persistent
        ).apply {
            description = "Keeps Guardian Saathi running in background"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        Log.d("GuardianService", "trying to build notification")
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian Saathi Active")
            .setContentText("You are protected")
            .setSmallIcon(R.drawable.ic_guardian)
            .setContentIntent(pendingIntent)
            .setOngoing(true)           // Can't be swiped away
            .setSilent(true)
            .setAutoCancel(false)
            .setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE  // Shows instantly, no delay
            )
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // KEY FIX 2: If system kills the service, restart it
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartIntent = Intent(applicationContext, GuardianService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        getSystemService(AlarmManager::class.java)?.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 2000, // restart after 2s
            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" // Xiaomi/OnePlus
        ) {
            // Only start if user was an Elder (check SharedPreferences or Room DB)
            val prefs = context.getSharedPreferences("guardian_saathi_prefs", Context.MODE_PRIVATE)
            val isElder = prefs.getString("user_role", null) == "ELDER"
            if (isElder) {
                GuardianService.start(context)
            }
        }
    }
}