package com.biprangshu.guardiansathi.Global.core

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GuardianSaathiFCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // save to realtime db whenever token refreshes
        saveTokenToDatabase(token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message from: ${remoteMessage.from}")
        Log.d("FCM", "Data: ${remoteMessage.data}")
        // handle incoming messages here later

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Guardian Saathi"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You may have recent alerts"

        val type = remoteMessage.data["type"] ?: "GENERAL"

        NotificationHelper(this).showNotification(title,body,type)
    }

    private fun saveTokenToDatabase(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        db.child(uid)
            .child("device_token")
            .setValue(token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to save token: ${e.message}")
            }
    }
}