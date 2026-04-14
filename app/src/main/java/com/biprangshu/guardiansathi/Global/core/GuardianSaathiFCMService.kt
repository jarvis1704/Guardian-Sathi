package com.biprangshu.guardiansathi.Global.core

import android.util.Log
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message from: ${remoteMessage.from}")
        Log.d("FCM", "Data: ${remoteMessage.data}")
        // handle incoming messages here later
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