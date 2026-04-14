package com.biprangshu.guardiansathi.Elder.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class FallDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_FALL_DISMISS") {
            Log.d("FallDismissReceiver", "✅ User dismissed fall alert — I'm okay")
            GuardianService.cancelFallNotification(context)
            // If the activity is open, it will handle its own cleanup via onDestroy
        }
    }
}