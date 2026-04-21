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
import com.biprangshu.guardiansathi.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Elder.ui.FallAlarmActivity
import com.biprangshu.guardiansathi.Global.MainActivity
import com.biprangshu.guardiansathi.R
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.jvm.java


@AndroidEntryPoint
class GuardianService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Inject lateinit var firebaseRepository: ElderFirebaseRepository
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "GUARDIAN_SERVICE_CHANNEL"

        const val FALL_ALERT_CHANNEL_ID = "FALL_ALERT_CHANNEL"
        const val FALL_NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, GuardianService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardianService::class.java))
        }

        fun cancelFallNotification(context: Context) {
            context.getSystemService(NotificationManager::class.java)
                ?.cancel(FALL_NOTIFICATION_ID)
        }
    }

    private lateinit var fallDetector: FallDetector

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createFallAlertChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        fallDetector = FallDetector(this){
            onFallDetected()
        }
        fallDetector.start()
    }

    private fun onFallDetected() {
        Log.w("GuardianService", "🚨 FALL DETECTED")

        //check permissions
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val canUse = nm?.canUseFullScreenIntent() ?: false
            Log.w("GuardianService", "canUseFullScreenIntent: $canUse")
            if (!canUse) {
                Log.e("GuardianService", "❌ Full screen intent permission NOT granted — this is why activity isn't launching")
            }
        }

        // Create intent for the alarm activity
        val fullScreenIntent = Intent(this, FallAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, FallDismissReceiver::class.java).apply {
            action = "ACTION_FALL_DISMISS"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FALL_ALERT_CHANNEL_ID)
            .setContentTitle(getString(R.string.FallAlert_4))
            .setContentText(getString(R.string.FallAlert_5))
            .setSmallIcon(R.drawable.ic_guardian)
            .setPriority(NotificationCompat.PRIORITY_MAX)          // must be MAX
            .setCategory(NotificationCompat.CATEGORY_CALL)         // CATEGORY_CALL gets special treatment
            .setFullScreenIntent(fullScreenPendingIntent, true)    // true = high urgency
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_guardian,
                    getString(R.string.FallAlert_3),
                    dismissPendingIntent
                ).build()
            )
            .build()

        getSystemService(NotificationManager::class.java)
            ?.notify(FALL_NOTIFICATION_ID, notification)
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
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().uid
            var wasInsideGeofence: Boolean? = null
            
            while (isActive) {
                val location = getLastKnownLocation(this@GuardianService)
                val lat = location?.first?:0.0
                val long = location?.second?:0.0

                if (lat !=0.0 && long !=0.0){
                    firebaseRepository.sendDataToFirebaseDatabase("location_lat",lat.toString())
                    firebaseRepository.sendDataToFirebaseDatabase("location_long",long.toString())
                    firebaseRepository.updateFirebaseTimestamp("location_lastSeen")
                    
                    if (uid != null) {
                        try {
                            val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                                .child(uid)
                                .child("geofence")
                                .get()
                                .await()
                            
                            val isActiveGeofence = snapshot.child("is_active").getValue(Boolean::class.java) ?: false
                            if (isActiveGeofence) {
                                val centerLat = snapshot.child("center_lat").getValue(Double::class.java)
                                val centerLng = snapshot.child("center_long").getValue(Double::class.java)
                                val radiusMeters = snapshot.child("radius_meters").getValue(Double::class.java)
                                
                                if (centerLat != null && centerLng != null && radiusMeters != null) {
                                    val results = FloatArray(1)
                                    android.location.Location.distanceBetween(
                                        lat, long,
                                        centerLat, centerLng,
                                        results
                                    )
                                    val distance = results[0]
                                    val isInsideCurrently = distance <= radiusMeters
                                    
                                    if (wasInsideGeofence != null && wasInsideGeofence != isInsideCurrently) {
                                        val type = if (isInsideCurrently) "GEOFENCE_ENTER" else "GEOFENCE_EXIT"
                                        firebaseRepository.pushActivityLog(type)
                                    }
                                    wasInsideGeofence = isInsideCurrently
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GuardianService", "Error checking geofence: ${e.message}")
                        }
                    }
                }

                delay(3 * 60 * 1000L)
            }
        }

        serviceScope.launch {
            while (isActive) {
                val batterydata = readBattery(this@GuardianService)
                firebaseRepository.sendDataToFirebaseDatabase("battery_level",batterydata.level.toString())
                firebaseRepository.sendDataToFirebaseDatabase("battery_isCharging",batterydata.isCharging.toString())
                firebaseRepository.updateFirebaseTimestamp("battery_lastSeen")
                delay(5 * 60 * 1000L)
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

    private fun createFallAlertChannel() {
        val channel = NotificationChannel(
            FALL_ALERT_CHANNEL_ID,
            "Fall Alerts",
            NotificationManager.IMPORTANCE_HIGH   // must be HIGH, not LOW
        ).apply {
            description = "Emergency fall detection alerts"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
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
        fallDetector.stop()
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