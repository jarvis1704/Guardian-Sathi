package com.biprangshu.guardiansathi.Global.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.biprangshu.guardiansathi.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_EMERGENCY = "channel_emergency"
        const val CHANNEL_REMINDER  = "channel_reminder"
        const val CHANNEL_GENERAL   = "channel_general"

        fun createChannels(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)

            listOf(
                NotificationChannel(
                    CHANNEL_EMERGENCY, "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Fall detection, SOS alerts"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = Color.RED
                },
                NotificationChannel(CHANNEL_REMINDER, "Medicine Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Medicine and health reminders"
                },
                NotificationChannel(CHANNEL_GENERAL, "General",
                    NotificationManager.IMPORTANCE_LOW).apply {
                    description = "General app notifications"
                }
            ).forEach { manager.createNotificationChannel(it) }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(title: String, body: String, type: String) {
        val (channelId, priority, smallIcon) = when (type) {
            "FALL", "SOS", "EMERGENCY" ->
                Triple(CHANNEL_EMERGENCY, NotificationCompat.PRIORITY_MAX, R.drawable.ic_logo_bare)
            "MEDICINE" ->
                Triple(CHANNEL_REMINDER, NotificationCompat.PRIORITY_DEFAULT, R.drawable.ic_logo_bare)
            else ->
                Triple(CHANNEL_GENERAL, NotificationCompat.PRIORITY_LOW, R.drawable.ic_logo_bare)
        }

        // Deep-link intent — opens app on tap
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}