package com.biprangshu.guardiansathi.Global.Elder.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.biprangshu.guardiansathi.Global.MainActivity
import com.biprangshu.guardiansathi.R

class MedicineAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "MEDICINE_REMINDER_CHANNEL"
        const val NOTIFICATION_BASE_ID = 3000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val medicineDosage = intent.getStringExtra("medicine_dosage") ?: ""
        val instructions = intent.getStringExtra("instructions") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notificationId = NOTIFICATION_BASE_ID + reminderId.hashCode()

        val takenIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_MEDICINE_TAKEN"
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("medicine_dosage", medicineDosage)
            putExtra("notification_id", notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_MEDICINE_SKIP"
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("medicine_dosage", medicineDosage)
            putExtra("notification_id", notificationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context, notificationId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_guardian)
            .setContentTitle("Medicine Reminder: $medicineName")
            .setContentText("Time to take $medicineDosage. $instructions")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_guardian, "TAKEN", takenPendingIntent)
            .addAction(R.drawable.ic_guardian, "SKIP", skipPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medicine schedules"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
