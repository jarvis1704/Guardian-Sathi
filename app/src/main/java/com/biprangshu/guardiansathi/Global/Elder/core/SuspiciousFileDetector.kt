package com.biprangshu.guardiansathi.Global.Elder.core

enum class FileConfidence {
    LOW,
    MEDIUM,
    HIGH
}

data class FileDetectionResult(
    val isSuspicious: Boolean,
    val matchedFileName: String? = null,
    val confidence: FileConfidence? = null
)

class SuspiciousFileDetector {

    // Regex to detect files with specific dangerous or archive extensions
    private val fileRegex = Regex(
        "\\b[a-zA-Z0-9_\\-\\.]+\\.(apk|exe|bat|msi|scr|vbs|cmd|ps1|sh|jar|zip|rar|7z)\\b",
        RegexOption.IGNORE_CASE
    )

    // Regex to check for double extensions ending in zip/rar/7z/apk (e.g. document.pdf.zip or image.png.apk)
    private val doubleExtensionRegex = Regex(
        "\\.[a-zA-Z0-9]+\\.(apk|exe|bat|msi|scr|vbs|cmd|ps1|sh|jar|zip|rar|7z)$",
        RegexOption.IGNORE_CASE
    )

    private val remoteAccessTools = listOf(
        "anydesk",
        "teamviewer",
        "rustdesk",
        "airdroid",
        "zoho assist",
        "logmein",
        "splashtop",
        "chrome remote desktop",
        "vnc viewer"
    )

    private val highConfidenceExtensions = setOf(
        "apk", "exe", "bat", "msi", "scr", "vbs", "cmd", "ps1", "sh", "jar"
    )

    private val mediumConfidenceExtensions = setOf(
        "zip", "rar", "7z"
    )

    private val lowConfidenceKeywords = listOf(
        "download-update",
        "install-update",
        "install-patch",
        "update-now",
        "patch-security",
        "verify-identity",
        "system-update",
        "update.apk",
        "security-update"
    )

    fun detectSuspiciousFile(text: String): FileDetectionResult {
        val candidates = extractFileCandidates(text)
        
        var highestConfidence: FileConfidence? = null
        var matchedFile: String? = null

        // 1. Check extracted file candidates first
        for (candidate in candidates) {
            val lowercaseCandidate = candidate.lowercase()
            val extension = lowercaseCandidate.substringAfterLast(".", "")

            var confidence: FileConfidence? = null

            // Check if extension is high confidence
            if (highConfidenceExtensions.contains(extension)) {
                confidence = FileConfidence.HIGH
            }
            // Check if it's a remote access tool name
            else if (remoteAccessTools.any { tool -> lowercaseCandidate.contains(tool) }) {
                confidence = FileConfidence.HIGH
            }
            // Check if extension is medium confidence
            else if (mediumConfidenceExtensions.contains(extension)) {
                confidence = FileConfidence.MEDIUM
            }

            if (confidence != null) {
                if (highestConfidence == null || confidence.ordinal > highestConfidence.ordinal) {
                    highestConfidence = confidence
                    matchedFile = candidate
                }
            }
        }

        // 2. If no candidate file with extensions was matched, check if the raw text contains remote access tool names
        if (highestConfidence == null) {
            val lowercaseText = text.lowercase()
            for (tool in remoteAccessTools) {
                if (lowercaseText.contains(tool)) {
                    highestConfidence = FileConfidence.HIGH
                    matchedFile = tool
                    break
                }
            }
        }

        // 3. Scan for low-confidence download keywords in the raw text
        if (highestConfidence == null) {
            val lowercaseText = text.lowercase()
            for (keyword in lowConfidenceKeywords) {
                val formattedKeyword = keyword.replace("-", " ")
                if (lowercaseText.contains(formattedKeyword) || lowercaseText.contains(keyword)) {
                    highestConfidence = FileConfidence.LOW
                    matchedFile = keyword
                    break
                }
            }
        }

        return if (highestConfidence != null) {
            FileDetectionResult(
                isSuspicious = true,
                matchedFileName = matchedFile,
                confidence = highestConfidence
            )
        } else {
            FileDetectionResult(isSuspicious = false)
        }
    }

    private fun extractFileCandidates(text: String): List<String> {
        val candidates = mutableListOf<String>()
        val matches = fileRegex.findAll(text)
        for (match in matches) {
            candidates.add(match.value)
        }
        return candidates
    }
}
