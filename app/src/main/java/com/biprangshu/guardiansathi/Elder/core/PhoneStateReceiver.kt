package com.biprangshu.guardiansathi.Elder.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.biprangshu.guardiansathi.Elder.data.ElderFirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class PhoneStateReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var firebaseRepository: ElderFirebaseRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("PhoneStateReceiver", "📱 Phone state changed: $state, number: $incomingNumber")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call - THIS WILL NOW TRIGGER
                    val phoneNumber = incomingNumber ?: "Unknown"
                    Log.d("PhoneStateReceiver", "📞 INCOMING CALL from: $phoneNumber")

                    val isUnknown = isUnknownNumber(context, phoneNumber)

                    Log.d("PhoneStateReceiver", "Is unknown number: $isUnknown")

                    if (isUnknown) {
                        scope.launch {
                            try {
                                val newNotifData = NotificationData(
                                    packageName = "Phone",
                                    appName = "Phone",
                                    title = "Unknown caller Detected",
                                    desc = "An unknown caller is detected, number: $phoneNumber",
                                    body = "Unknown caller Detected: $phoneNumber",
                                    timestamp = 0
                                )
                                firebaseRepository.sendNotificaitonToGuardian(newNotifData, false, false, "MID")
                            } catch (e: Exception) {
                                Log.e("PhoneStateReceiver", "Failed to send alert", e)
                            }
                        }
                    }
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("PhoneStateReceiver", "✅ Call answered")
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d("PhoneStateReceiver", "📵 Call ended")
                }
            }
        }
    }

    private fun isUnknownNumber(context: Context, phoneNumber: String): Boolean {
        if (phoneNumber == "Unknown" || phoneNumber.isBlank()) return true

        return try {
            val uri = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            val contactUri = android.net.Uri.withAppendedPath(uri, android.net.Uri.encode(phoneNumber))

            val cursor = context.contentResolver.query(
                contactUri,
                arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )

            val isUnknown = cursor?.count == 0
            cursor?.close()

            isUnknown
        } catch (e: Exception) {
            Log.e("PhoneStateReceiver", "Error checking contact", e)
            true // Assume unknown if error
        }
    }
}