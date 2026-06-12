package com.biprangshu.guardiansathi.Global.Elder.core

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentAccessibilityService : AccessibilityService() {

    @Inject lateinit var firebaseRepository: ElderFirebaseRepository

    private lateinit var overlayManager: OverlayManager

    // Debounce state — prevent re-alerting for the same payment screen
    private var lastAlertedAmount: Double = -1.0
    private var lastAlertTime: Long = 0L
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

        val paymentInfo = PaymentDetector.extractPaymentInfo(
            rootNode = rootNode,
            packageName = packageName,
            threshold = paymentThreshold
        ) ?: return

        val now = System.currentTimeMillis()
        if (paymentInfo.amount == lastAlertedAmount && (now - lastAlertTime) < DEBOUNCE_MS) {
            return
        }

        lastAlertedAmount = paymentInfo.amount
        lastAlertTime = now

        val message = buildMessage(paymentInfo)

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

    private fun buildMessage(info: PaymentDetector.PaymentInfo): String {
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
