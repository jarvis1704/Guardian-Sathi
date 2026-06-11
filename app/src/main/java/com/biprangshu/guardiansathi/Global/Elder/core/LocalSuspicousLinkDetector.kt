package com.biprangshu.guardiansathi.Global.Elder.core

enum class LinkConfidence {
    LOW,
    MEDIUM,
    HIGH
}

//the links are categorised as high, low and medium based on confidence. High and medium are shown as popup, low is ignored.
data class SuspiciousLinkInfo(
    val pattern: String,
    val confidence: LinkConfidence
)

data class LinkDetectionResult(
    val isSuspicious: Boolean,
    val matchedLink: String? = null,
    val confidence: LinkConfidence? = null
)

class LocalSuspicousLinkDetector {

    // Regex to detect links in text (handles http/https/www or naked domains with paths)
    private val urlRegex = Regex(
        "\\b(?:https?://|www\\.)?[a-zA-Z0-9-]{1,63}\\.[a-zA-Z]{2,6}\\b(?:/[^\\s]*)?",
        RegexOption.IGNORE_CASE
    )

    private val suspiciousList = listOf(
        // HIGH confidence: Known high-risk phishing/scam domains and terms
        SuspiciousLinkInfo("free-rewards", LinkConfidence.HIGH),
        SuspiciousLinkInfo("win-lottery", LinkConfidence.HIGH),
        SuspiciousLinkInfo("kyc-update", LinkConfidence.HIGH),
        SuspiciousLinkInfo("verify-bank", LinkConfidence.HIGH),
        SuspiciousLinkInfo("claim-gift", LinkConfidence.HIGH),
        SuspiciousLinkInfo("unclaimed-funds", LinkConfidence.HIGH),
        SuspiciousLinkInfo("account-blocked", LinkConfidence.HIGH),
        SuspiciousLinkInfo("netf1ix", LinkConfidence.HIGH),
        SuspiciousLinkInfo("paytm-cashback", LinkConfidence.HIGH),
        SuspiciousLinkInfo("gpay-rewards", LinkConfidence.HIGH),
        SuspiciousLinkInfo("whatsapp-cashback", LinkConfidence.HIGH),
        SuspiciousLinkInfo("secure-login-update", LinkConfidence.HIGH),
        SuspiciousLinkInfo("phishing-test-high.com", LinkConfidence.HIGH),

        // MEDIUM confidence: Common URL shorteners or suspicious generic keywords
        SuspiciousLinkInfo("bit.ly", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("tinyurl.com", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("shorturl.at", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("t.co", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("cutt.ly", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("rebrand.ly", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("free-promo", LinkConfidence.MEDIUM),
        SuspiciousLinkInfo("phishing-test-mid.com", LinkConfidence.MEDIUM),

        // LOW confidence: Generic suspicious keywords that could be false positives
        SuspiciousLinkInfo("test-low", LinkConfidence.LOW),
        SuspiciousLinkInfo("low-risk-suspicious", LinkConfidence.LOW)
    )

    fun detectSuspiciousLink(text: String): LinkDetectionResult {
        val links = extractLinks(text)
        if (links.isEmpty()) {
            return LinkDetectionResult(isSuspicious = false)
        }

        var highestConfidence: LinkConfidence? = null
        var matchedUrl: String? = null

        for (link in links) {
            val lowercaseLink = link.lowercase()
            for (suspiciousInfo in suspiciousList) {
                if (lowercaseLink.contains(suspiciousInfo.pattern.lowercase())) {
                    val currentConfidence = suspiciousInfo.confidence
                    //update the confidence after iterating through the list of scamy sites
                    if (highestConfidence == null || currentConfidence.ordinal > highestConfidence.ordinal) {
                        highestConfidence = currentConfidence
                        matchedUrl = link
                    }
                }
            }
        }

        //only returnes if highest confidence is gained, or else flags as false
        return if (highestConfidence != null) {
            LinkDetectionResult(
                isSuspicious = true,
                matchedLink = matchedUrl,
                confidence = highestConfidence
            )
        } else {
            LinkDetectionResult(isSuspicious = false)
        }
    }

    //helper function to extract links using regex
    fun extractLinks(text: String): List<String> {
        val links = mutableListOf<String>()
        val matches = urlRegex.findAll(text)
        for (match in matches) {
            links.add(match.value)
        }
        return links
    }
}
