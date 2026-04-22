package com.biprangshu.guardiansathi.Elder.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.biprangshu.guardiansathi.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Elder.data.local.ElderNotification
import com.biprangshu.guardiansathi.Elder.data.local.ElderNotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ElderNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var firebaseRepository: ElderFirebaseRepository

    @Inject
    lateinit var elderRoomRepository: ElderNotificationRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            val packageName = notification.packageName
            val extras = notification.notification.extras

            // Extract notification details
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val subText = extras.getCharSequence("android.subText")?.toString() ?: ""

            // Filter for important apps (SMS, WhatsApp, calls, banking, etc.)
            val importantApps = listOf(
                "com.android.messaging",        // SMS
                "com.google.android.apps.messaging", // Google Messages
                "com.whatsapp",                 // WhatsApp
                "com.truecaller",               // Truecaller
                "com.phonepe.app",              // PhonePe
                "com.paytm",                    // Paytm
                "in.org.npci.upiapp",          // BHIM UPI
                "com.google.android.apps.nbu.paisa.user", // Google Pay
                "com.android.phone",            // Phone app
                "com.phonepe.app"               // Banking apps pattern
            )

            if (packageName in importantApps || packageName.contains("bank")) {
                val notificationData = NotificationData(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    title = title,
                    desc = text,
                    body = subText,
                    timestamp = System.currentTimeMillis()
                )

                Log.d("NotificationListener", "Captured: ${notificationData.appName} - $title: $text")

                // check if its an OTP
                val otpResult = detectOtp(notificationData.title, notificationData.desc)
                val transactionResult = detectTransaction(notificationData.title, notificationData.desc)

                if (otpResult.isOtp){
                    Log.d("NotificationListener", "OTP Detected: ${otpResult.otpValue}")
                    //immediately send to firebase
                    sendNotificationToFirebase(notificationData, true)

                }else if (transactionResult.isTransaction && transactionResult.amount!=null) {
                    //immediately send to firebase if amount is greater than a set value
                    if (transactionResult.amount >= 100){
                        val newNotifData = NotificationData(
                            packageName = notificationData.packageName,
                            appName = notificationData.appName,
                            title = transactionResult.type.toString()+" Transaction Detected",
                            desc = "Amount: ${transactionResult.amount}",
                            body = notificationData.body+" | "+notificationData.desc,
                            timestamp = notificationData.timestamp
                        )
                        sendNotificationToFirebase(newNotifData, false, true)
                    }
                }
                else{
                    val notif = ElderNotification(
                        title = notificationData.title,
                        body = notificationData.body,
                        desc = notificationData.desc,
                        imp = "",
                        appName = notificationData.appName,
                        time = notificationData.timestamp
                    )
                    serviceScope.launch {
                        elderRoomRepository.insertNotification(notif)
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Optional: Track when notifications are dismissed
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun isPotentialScam(title: String, text: String): Boolean {
        val combinedText = "$title $text".lowercase()

        val scamKeywords = listOf(
            "otp",
            "verify",
            "urgent",
            "account blocked",
            "suspended",
            "confirm your",
            "click here",
            "link expires",
            "prize",
            "congratulations",
            "won",
            "claim now",
            "limited time",
            "act now",
            "কনফার্ম", // Bengali for confirm
            "ওটিপি",   // Bengali for OTP
            "तुरंत",    // Hindi for urgent
        )

        return scamKeywords.any { keyword -> combinedText.contains(keyword) }
    }

    private fun sendNotificationToFirebase(data: NotificationData, isOtp: Boolean, isTransaction: Boolean = false) {
        serviceScope.launch {
            try {
                firebaseRepository.sendNotificaitonToGuardian(data, isOtp, isTransaction)
//                firebaseRepository.sendNotificationLog(
//                    mapOf(
//                        "app" to data.appName,
//                        "title" to data.title,
//                        "text" to data.text,
//                        "timestamp" to data.timestamp,
//                        "packageName" to data.packageName
//                    )
//                )
            } catch (e: Exception) {
                Log.e("NotificationListener", "Failed to send to Firebase", e)
            }
        }
    }

    private fun alertGuardianOfScam(data: NotificationData) {
        serviceScope.launch {
            try {
//                firebaseRepository.sendScamAlert(
//                    mapOf(
//                        "type" to "suspicious_notification",
//                        "app" to data.appName,
//                        "title" to data.title,
//                        "text" to data.text,
//                        "timestamp" to System.currentTimeMillis(),
//                        "severity" to "high"
//                    )
//                )
            } catch (e: Exception) {
                Log.e("NotificationListener", "Failed to send scam alert", e)
            }
        }
    }
}

data class NotificationData(
    val packageName: String,
    val appName: String,
    val title: String,
    val desc: String,
    val body: String,
    val timestamp: Long
)