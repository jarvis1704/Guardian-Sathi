package com.biprangshu.guardiansathi.Elder.core

private val transactionKeywords = setOf(
    // English
    "debited", "credited", "paid", "received", "transferred", "withdrawn",
    "deposit", "payment", "transaction", "spent", "charged", "refund",
    "cashback", "balance", "amount", "rs.", "inr", "₹",

    // Hindi
    "डेबिट", "क्रेडिट", "भुगतान", "प्राप्त", "स्थानांतरित", "निकाला",
    "जमा", "राशि", "लेनदेन", "खर्च", "वापसी", "शेष राशि",

    // Bengali
    "ডেবিট", "ক্রেডিট", "পরিশোধ", "প্রাপ্ত", "স্থানান্তর",
    "জমা", "পরিমাণ", "লেনদেন", "ফেরত",

    // Tamil
    "பற்று", "வரவு", "செலுத்தல்", "பெற்றது", "பரிமாற்றம்",
    "டெபாசிட்", "தொகை", "பரிவர்த்தனை",

    // Telugu
    "డెబిట్", "క్రెడిట్", "చెల్లింపు", "స్వీకరించారు", "బదిలీ",
    "డిపాజిట్", "మొత్తం", "లావాదేవీ",

    // Marathi
    "डेबिट", "क्रेडिट", "भरणा", "प्राप्त", "हस्तांतरित",
    "ठेव", "रक्कम", "व्यवहार",

    // Gujarati
    "ડેબિટ", "ક્રેડિટ", "ચૂકવણી", "પ્રાપ્ત", "ટ્રાન્સફર",
    "જમા", "રકમ", "વ્યવહાર",

    // Kannada
    "ಡೆಬಿಟ್", "ಕ್ರೆಡಿಟ್", "ಪಾವತಿ", "ಸ್ವೀಕರಿಸಿದ", "ವರ್ಗಾವಣೆ",
    "ಠೇವಣಿ", "ಮೊತ್ತ", "ವಹಿವಾಟು",

    // Malayalam
    "ഡെബിറ്റ്", "ക്രെഡിറ്റ്", "പണം നൽകി", "ലഭിച്ചു", "കൈമാറ്റം",
    "നിക്ഷേപം", "തുക", "ഇടപാട്",

    // Punjabi
    "ਡੈਬਿਟ", "ਕ੍ਰੈਡਿਟ", "ਭੁਗਤਾਨ", "ਪ੍ਰਾਪਤ", "ਟ੍ਰਾਂਸਫਰ",
    "ਜਮ੍ਹਾ", "ਰਕਮ", "ਲੈਣ-ਦੇਣ",

    // Odia
    "ଡେବିଟ", "କ୍ରେଡିଟ", "ଦେୟ", "ପ୍ରାପ୍ତ", "ସ୍ଥାନାନ୍ତର",
    "ଜମା", "ପରିମାଣ", "କାରବାର",

    // Urdu
    "ڈیبٹ", "کریڈٹ", "ادائیگی", "موصول", "منتقلی",
    "رقم", "لین دین",

    // Assamese
    "ডেবিট", "ক্ৰেডিট", "পৰিশোধ", "প্ৰাপ্ত", "স্থানান্তৰ",
    "জমা", "পৰিমাণ", "লেনদেন"
)

private val amountPatterns = listOf(
    // ₹1,234.56 or Rs.1234
    Regex("""[₹Rs.INR\s]+[\s]?(\d{1,10}(?:,\d{3})*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
    // 1234.56 INR
    Regex("""(\d{1,10}(?:,\d{3})*(?:\.\d{1,2})?)\s*(?:INR|RS|RUPEES)""", RegexOption.IGNORE_CASE),
    // "amount 5000"
    Regex("""(?:amount|amt)[^\d]{0,5}(\d{1,10}(?:,\d{3})*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
)

private fun extractAmount(text: String): Double? {
    amountPatterns.forEach { pattern ->
        val match = pattern.find(text)?.groupValues?.getOrNull(1)
        if (match != null) {
            return match.replace(",", "").toDoubleOrNull()
        }
    }
    return null
}

enum class TransactionType { DEBIT, CREDIT, UNKNOWN }

data class TransactionDetectionResult(
    val isTransaction: Boolean,
    val type: TransactionType = TransactionType.UNKNOWN,
    val amount: Double? = null,
    val confidence: TransactionConfidence = TransactionConfidence.NONE
)

enum class TransactionConfidence { NONE, LOW, MEDIUM, HIGH }

private val debitKeywords = setOf(
    "debited", "paid", "spent", "withdrawn", "deducted", "charged",
    "डेबिट", "भुगतान", "डेबिट किया", "काटा गया",
    "ডেবিট", "পরিশোধ",
    "பற்று", "செலுத்தல்",
    "డెబిట్", "చెల్లింపు",
    "ಡೆಬಿಟ್", "ಪಾವತಿ",
    "ഡെബിറ്റ്",
    "ਡੈਬਿਟ",
    "ডেবিট"
)

private val creditKeywords = setOf(
    "credited", "received", "deposited", "cashback", "refund", "added",
    "क्रेडिट", "प्राप्त", "जमा", "वापसी",
    "ক্রেডিট", "প্রাপ্ত", "জমা",
    "வரவு", "பெற்றது",
    "క్రెడిట్", "స్వీకరించారు",
    "ಕ್ರೆಡಿಟ್", "ಸ್ವೀಕರಿಸಿದ",
    "ക്രെഡിറ്റ്", "ലഭിച്ചു",
    "ਕ੍ਰੈਡਿਟ", "ਪ੍ਰਾਪਤ",
    "ক্ৰেডিট", "প্ৰাপ্ত"
)


fun detectTransaction(title: String, body: String): TransactionDetectionResult {
    val combined = "$title $body"
    val lower = combined.lowercase()

    val hasTransactionKeyword = transactionKeywords.any {
        combined.contains(it, ignoreCase = true)
    }

    if (!hasTransactionKeyword) {
        return TransactionDetectionResult(isTransaction = false)
    }

    val amount = extractAmount(combined)

    val type = when {
        debitKeywords.any { combined.contains(it, ignoreCase = true) } -> TransactionType.DEBIT
        creditKeywords.any { combined.contains(it, ignoreCase = true) } -> TransactionType.CREDIT
        else -> TransactionType.UNKNOWN
    }

    // HIGH: keyword + amount + clear debit/credit direction
    // MEDIUM: keyword + amount but direction unclear
    // LOW: keyword only, no amount found
    val confidence = when {
        hasTransactionKeyword && amount != null && type != TransactionType.UNKNOWN -> TransactionConfidence.HIGH
        hasTransactionKeyword && amount != null -> TransactionConfidence.MEDIUM
        hasTransactionKeyword -> TransactionConfidence.LOW
        else -> TransactionConfidence.NONE
    }

    return TransactionDetectionResult(
        isTransaction = true,
        type = type,
        amount = amount,
        confidence = confidence
    )
}