package com.biprangshu.guardiansathi.Global.Elder.core

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuardianAccessibilityService : AccessibilityService() {

    @Inject lateinit var firebaseRepository: ElderFirebaseRepository

    private lateinit var overlayManager: OverlayManager
    private val suspiciousFileDetector = SuspiciousFileDetector()

    // Debounce state for payments — prevent re-alerting for the same payment screen
    private var lastAlertedAmount: Double = -1.0
    private var lastPaymentAlertTime: Long = 0L

    // Debounce state for files — prevent re-alerting for the same file
    private var lastAlertedFileName: String? = null
    private var lastFileAlertTime: Long = 0L

    private val DEBOUNCE_MS = 3000L

    // TODO: read this from user preferences / shared settings
    private val paymentThreshold: Double = 500.0

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this, firebaseRepository)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return
        val rootNode = rootInActiveWindow ?: return

        // 1. Payment detection (restricted to UPI/payment apps)
        if (isPaymentApp(packageName)) {
            val paymentInfo = PaymentDetector.extractPaymentInfo(
                rootNode = rootNode,
                packageName = packageName,
                threshold = paymentThreshold
            )
            if (paymentInfo != null) {
                val now = System.currentTimeMillis()
                if (paymentInfo.amount != lastAlertedAmount || (now - lastPaymentAlertTime) >= DEBOUNCE_MS) {
                    lastAlertedAmount = paymentInfo.amount
                    lastPaymentAlertTime = now

                    val message = buildPaymentMessage(paymentInfo)

                    Handler(Looper.getMainLooper()).post {
                        overlayManager.showSuspiciousLinkOverlay(
                            message = message,
                            title = getString(R.string.suspicious_payment_title),
                            onDismiss = {
                                // TODO: send acknowledgement to guardian via FirebaseRepository
                                // TODO: log this payment alert event for the guardian's dashboard
                            }
                        )
                    }
                }
            }
        }

        // 2. Suspicious file detection
        val allText = mutableListOf<String>()
        collectAllText(rootNode, allText)
        val fullText = allText.joinToString(" ")

        val fileResult = suspiciousFileDetector.detectSuspiciousFile(fullText)
        if (fileResult.isSuspicious && fileResult.matchedFileName != null && fileResult.confidence != null) {
            val now = System.currentTimeMillis()
            if (fileResult.matchedFileName != lastAlertedFileName || (now - lastFileAlertTime) >= DEBOUNCE_MS) {
                lastAlertedFileName = fileResult.matchedFileName
                lastFileAlertTime = now

                if (fileResult.confidence == FileConfidence.HIGH || fileResult.confidence == FileConfidence.MEDIUM) {
                    val threatLevel = when (fileResult.confidence) {
                        FileConfidence.HIGH -> "High"
                        FileConfidence.MEDIUM -> "Medium"
                        FileConfidence.LOW -> "Low"
                    }
                    val message = getString(
                        R.string.suspicious_file_overlay_message,
                        fileResult.matchedFileName,
                        threatLevel
                    )

                    Handler(Looper.getMainLooper()).post {
                        overlayManager.showSuspiciousLinkOverlay(
                            message = message,
                            title = getString(R.string.suspicious_file_title),
                            onDismiss = {
                                // optional dismiss actions
                            }
                        )
                    }

                    // Send notification to guardian via Firebase immediately
                    val notifData = NotificationData(
                        packageName = packageName,
                        appName = packageName.toAppName(),
                        title = getString(R.string.suspicious_file_title),
                        desc = "A suspicious file was detected on screen: ${fileResult.matchedFileName}",
                        body = fullText,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    try {
                        val importanceStr = when (fileResult.confidence) {
                            FileConfidence.HIGH -> "HIGH"
                            FileConfidence.MEDIUM -> "MID"
                            FileConfidence.LOW -> "LOW"
                        }
                        firebaseRepository.sendNotificaitonToGuardian(notifData, false, false, importanceStr)
                    } catch (e: Exception) {
                        Log.e("GuardianAccessibility", "Failed to send file alert to Guardian: ${e.message}")
                    }
                }
            }
        }
    }

    private fun isPaymentApp(packageName: String): Boolean {
        return when (packageName) {
            "com.phonepe.app",
            "net.one97.paytm",
            "com.google.android.apps.nbu.paisa.user",
            "in.amazon.mShop.android.shopping",
            "com.mobikwik_new",
            "com.bhim.axis" -> true
            else -> false
        }
    }

    private fun String.toAppName(): String {
        return when (this) {
            "com.phonepe.app" -> "PhonePe"
            "net.one97.paytm" -> "Paytm"
            "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
            "in.amazon.mShop.android.shopping" -> "Amazon Pay"
            "com.mobikwik_new" -> "MobiKwik"
            "com.bhim.axis" -> "BHIM"
            "com.whatsapp" -> "WhatsApp"
            "com.whatsapp.w4b" -> "WhatsApp Business"
            "org.telegram.messenger" -> "Telegram"
            "com.google.android.apps.messaging" -> "Google Messages"
            "com.facebook.orca" -> "Messenger"
            else -> "App"
        }
    }


    //recursive function to get all text in screen and store them in list
    private fun collectAllText(node: AccessibilityNodeInfo, output: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectAllText(it, output) }
        }
    }

    private fun buildPaymentMessage(info: PaymentDetector.PaymentInfo): String {
        return buildString {
            append("You are about to pay ${info.amountRaw}")
            info.recipient?.let { append(" to $it") }
            info.upiId?.let { append(" (UPI: $it)") }
            append(" via ${info.appName}.")
            append("\n\nPlease verify the recipient and amount before proceeding.")
        }
    }

    override fun onInterrupt() {
        if (::overlayManager.isInitialized) {
            overlayManager.removeOverlay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayManager.isInitialized) {
            overlayManager.removeOverlay()
        }
    }
}
