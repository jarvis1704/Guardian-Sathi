package com.biprangshu.guardiansathi.Global.Elder.core

import android.view.accessibility.AccessibilityNodeInfo

object PaymentDetector {

    // Matches: ₹10,000 / Rs. 500 / INR 1000 / 1,000.00 ₹
    private val amountRegex = Regex(
        """(?:₹|Rs\.?|INR)\s*([\d,]+(?:\.\d{1,2})?)|([\d,]+(?:\.\d{1,2})?)\s*(?:₹|Rs\.?|INR)""",
        RegexOption.IGNORE_CASE
    )

    private val upiRegex = Regex("""[\w.\-]+@[\w]+""")

    data class PaymentInfo(
        val amount: Double,
        val amountRaw: String,
        val recipient: String?,
        val upiId: String?,
        val appName: String
    )

    fun extractPaymentInfo(
        rootNode: AccessibilityNodeInfo?,
        packageName: String,
        threshold: Double = 500.0
    ): PaymentInfo? {
        rootNode ?: return null

        val allText = mutableListOf<String>()
        collectAllText(rootNode, allText)
        val fullText = allText.joinToString(" ")

        val amountMatch = amountRegex.find(fullText) ?: return null
        val amountStr = (amountMatch.groupValues[1].ifEmpty { amountMatch.groupValues[2] })
            .replace(",", "")
        val amount = amountStr.toDoubleOrNull() ?: return null

        if (amount < threshold) return null

        val upiId = upiRegex.find(fullText)?.value

        val recipientRegex = Regex(
            """(?:pay(?:ing)?\s+to|to)\s+([A-Z][a-z]+(?:\s+[A-Z][a-z]+)*)""",
            RegexOption.IGNORE_CASE
        )
        val recipient = recipientRegex.find(fullText)?.groupValues?.get(1)

        return PaymentInfo(
            amount = amount,
            amountRaw = amountMatch.value.trim(),
            recipient = recipient,
            upiId = upiId,
            appName = packageName.toAppName()
        )
    }

    private fun collectAllText(
        node: AccessibilityNodeInfo,
        output: MutableList<String>
    ) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectAllText(it, output) }
        }
    }

    private fun String.toAppName() = when (this) {
        "com.phonepe.app" -> "PhonePe"
        "net.one97.paytm" -> "Paytm"
        "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
        "in.amazon.mShop.android.shopping" -> "Amazon Pay"
        "com.mobikwik_new" -> "MobiKwik"
        "com.bhim.axis" -> "BHIM"
        else -> "UPI App"
    }
}
