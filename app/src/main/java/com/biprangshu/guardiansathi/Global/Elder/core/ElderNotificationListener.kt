package com.biprangshu.guardiansathi.Global.Elder.core

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Global.Elder.data.local.ElderNotification
import com.biprangshu.guardiansathi.Global.Elder.data.local.ElderNotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class ElderNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var firebaseRepository: ElderFirebaseRepository

    @Inject
    lateinit var elderRoomRepository: ElderNotificationRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Deduplication: Track processed notifications
    private val processedNotifications = ConcurrentHashMap<String, Long>()
    private val DEDUP_WINDOW_MS = 5000L // 5 seconds

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            Log.d("ElderNotificationListener", "detected a notification")
            val packageName = notification.packageName
            val extras = notification.notification.extras

            // Create unique key for this notification
            val notificationKey = createNotificationKey(sbn)

            // Check if we've already processed this notification recently
            if (isDuplicate(notificationKey)) {
                Log.d("ElderNotificationListener", "⚠️ Skipping duplicate notification: $notificationKey")
                return
            }

            // Mark as processed
            markAsProcessed(notificationKey)

            // Extract notification details
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val subText = extras.getCharSequence("android.subText")?.toString() ?: ""

            // Skip if notification is empty or is a group summary
            if (title.isEmpty() && text.isEmpty()) {
                Log.d("ElderNotificationListener", "⚠️ Skipping empty notification")
                return
            }

            // Skip group summary notifications
            if (notification.isGroup && notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
                Log.d("ElderNotificationListener", "⚠️ Skipping group summary notification")
                return
            }

            // Filter for important apps (SMS, WhatsApp, calls, banking, etc.)
            val importantApps = setOf(
                // SMS
                "com.android.messaging",
                "com.google.android.apps.messaging",
                "com.samsung.android.messaging",
                "com.oneplus.mms",

                // WhatsApp family
                "com.whatsapp",
                "com.whatsapp.w4b",             // WhatsApp Business

                // Social / Messaging
                "com.facebook.katana",          // Facebook
                "com.facebook.lite",
                "com.facebook.orca",            // Messenger
                "com.instagram.android",
                "com.snapchat.android",
                "org.telegram.messenger",
                "org.telegram.plus",
                "com.viber.voip",
                "com.skype.raider",

                // UPI / Payments
                "com.phonepe.app",
                "com.paytm",
                "in.org.npci.upiapp",           // BHIM
                "com.google.android.apps.nbu.paisa.user", // GPay
                "net.one97.paytm",
                "com.amazon.mShop.android.shopping", // Amazon Pay
                "com.mobikwik_new",
                "com.freecharge.android",

                // Banks — Public
                "com.sbi.SBIFreedomPlus",
                "com.sbi.lotusintouch",
                "com.pnb.mbankingplus",
                "com.boi.Bank_of_India_Mobile_Banking",
                "com.csam.icici.bank.imobile",  // ICICI
                "com.snapwork.hdfc",            // HDFC
                "com.axis.mobile",
                "com.kotak.mobile.banking",
                "com.indusind.mobilebanking",
                "com.rbl.rblmobilebanking",
                "com.idbi.mPassbook",
                "com.myairtelapp",              // Airtel Payments Bank

                // Truecaller
                "com.truecaller",

                // OTP / Auth apps
                "com.google.android.apps.authenticator2",
                "com.authy.authy",
            )

            val excludeApps = listOf(
                "com.biprangshu.guardiansathi"
            )

            val appNameLower = getAppName(packageName).lowercase()

            val isDynamicallyImportant = listOf(
                "bank", "pay", "upi", "finance", "money", "wallet",
                "loan", "credit", "debit", "insurance", "mutual fund",
                "trading", "invest", "sbi", "hdfc", "icici", "axis",
                "kotak", "paytm", "phonepe", "gpay", "bhim"
            ).any { keyword -> appNameLower.contains(keyword) }

            if (packageName in importantApps || isDynamicallyImportant) {
                val notificationData = NotificationData(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    title = title,
                    desc = "",
                    body = text,
                    timestamp = System.currentTimeMillis()
                )

                Log.d("ElderNotificationListener", "Captured: ${notificationData.appName} - $title: $text")

                // check if its an OTP
                val otpResult = detectOtp(notificationData.title, notificationData.desc)
                val transactionResult = detectTransaction(notificationData.title, notificationData.desc)

                if (otpResult.isOtp){
                    Log.d("ElderNotificationListener", "OTP Detected: ${otpResult.otpValue}")
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
                    //store in room db for future checking
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
                        Log.d("ElderNotificationListener", "queing notification: $notif")
                    }
                }
            }
        }
    }

    private fun createNotificationKey(sbn: StatusBarNotification): String {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Create a unique key combining package, title, and text hash
        return "${sbn.packageName}:${title.hashCode()}:${text.hashCode()}"
    }

    private fun isDuplicate(key: String): Boolean {
        val lastProcessedTime = processedNotifications[key] ?: return false
        val timeSinceLastProcess = System.currentTimeMillis() - lastProcessedTime
        return timeSinceLastProcess < DEDUP_WINDOW_MS
    }

    private fun markAsProcessed(key: String) {
        processedNotifications[key] = System.currentTimeMillis()

        // Clean up old entries (keep map from growing indefinitely)
        if (processedNotifications.size > 100) {
            val now = System.currentTimeMillis()
            processedNotifications.entries.removeIf {
                now - it.value > DEDUP_WINDOW_MS * 2
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

    private fun sendNotificationToFirebase(data: NotificationData, isOtp: Boolean, isTransaction: Boolean = false) {
        serviceScope.launch {
            try {
                firebaseRepository.sendNotificaitonToGuardian(data, isOtp, isTransaction)
            } catch (e: Exception) {
                Log.e("NotificationListener", "Failed to send to Firebase", e)
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