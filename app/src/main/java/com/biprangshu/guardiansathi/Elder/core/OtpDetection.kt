package com.biprangshu.guardiansathi.Elder.core


private val otpKeywords = setOf(
    // English
    "otp", "one time password", "one-time password", "verification code",
    "passcode", "secure code", "auth code", "login code", "access code",

    // Hindi (Devanagari)
    "ओटीपी", "वन टाइम पासवर्ड", "सत्यापन कोड", "गुप्त कोड",

    // Bengali
    "ওটিপি", "যাচাই কোড", "গোপন কোড",

    // Tamil
    "ஓடிபி", "சரிபார்ப்பு குறியீடு",

    // Telugu
    "ఓటీపీ", "ధృవీకరణ కోడ్",

    // Marathi
    "ओटीपी", "सत्यापन संकेत",

    // Gujarati
    "ઓટીપી", "ચકાસણી કોડ",

    // Kannada
    "ಒಟಿಪಿ", "ಪರಿಶೀಲನೆ ಕೋಡ್",

    // Malayalam
    "ഒടിപി", "പരിശോധനാ കോഡ്",

    // Punjabi (Gurmukhi)
    "ਓਟੀਪੀ", "ਤਸਦੀਕ ਕੋਡ",

    // Odia
    "ଓଟିପି", "ଯାଞ୍ଚ କୋଡ",

    // Urdu (Arabic script)
    "او ٹی پی", "تصدیقی کوڈ",

    // Assamese
    "অ'টিপি", "যাচাই ক'ড",

    // Maithili
    "ओटीपी", "सत्यापन कोड",

    // Santali
    "ओटीपी",

    // Kashmiri
    "او ٹی پی",

    // Sindhi
    "او ٽي پي",

    // Konkani
    "ओटीपी",

    // Dogri
    "ओटीपी",

    // Manipuri (Meitei)
    "ꯑꯣꯇꯤꯄꯤ",

    // Bodo
    "ओटीपी",

    // Sanskrit
    "एकवारकूट"
)

private val otpPatterns = listOf(
    // "Your OTP is 123456"
    Regex("""(?:otp|code|pin|passcode)[^\d]{0,10}(\d{4,8})""", RegexOption.IGNORE_CASE),

    // "123456 is your OTP"
    Regex("""(\d{4,8})[^\d]{0,10}(?:otp|code|pin|passcode)""", RegexOption.IGNORE_CASE),

    // Standalone 4-8 digit numbers (common OTP lengths)
    Regex("""\b(\d{4,8})\b"""),

    // Alphanumeric OTPs like "AB1234"
    Regex("""\b([A-Z]{1,3}\d{4,6}|\d{4,6}[A-Z]{1,3})\b""")
)

data class OtpDetectionResult(
    val isOtp: Boolean,
    val otpValue: String? = null,       // the actual digits
    val detectedLanguage: String? = null,
    val confidence: OtpConfidence = OtpConfidence.NONE
)

enum class OtpConfidence { NONE, LOW, MEDIUM, HIGH }

fun detectOtp(title: String, body: String): OtpDetectionResult {
    val combined = "$title $body"

    // HIGH confidence: keyword + digit pattern both match
    val keywordFound = otpKeywords.any { keyword ->
        combined.contains(keyword, ignoreCase = true)
    }

    val digitMatch = otpPatterns.firstNotNullOfOrNull { pattern ->
        pattern.find(combined)?.groupValues?.getOrNull(1)
    }

    if (keywordFound && digitMatch != null) {
        return OtpDetectionResult(
            isOtp = true,
            otpValue = digitMatch,
            confidence = OtpConfidence.HIGH
        )
    }

    // MEDIUM confidence: only keyword found (OTP might be on next screen)
    if (keywordFound) {
        return OtpDetectionResult(
            isOtp = true,
            confidence = OtpConfidence.MEDIUM
        )
    }

    // LOW confidence: looks like an OTP by structure (4-8 digits in SMS/banking app)
    if (digitMatch != null && isFromTrustedOtpSender(combined)) {
        return OtpDetectionResult(
            isOtp = true,
            otpValue = digitMatch,
            confidence = OtpConfidence.LOW
        )
    }

    return OtpDetectionResult(isOtp = false)
}

// Bank/telecom SMS senders typically include these
private fun isFromTrustedOtpSender(text: String): Boolean {
    val otpSenderPatterns = listOf(
        Regex("""^[A-Z]{2}-[A-Z0-9]{6}"""),   // Indian SMS sender ID like "AX-HDFCBK"
        Regex("""VM-|BP-|JD-""")                // TRAI regulated sender prefixes
    )
    return otpSenderPatterns.any { it.containsMatchIn(text) }
}