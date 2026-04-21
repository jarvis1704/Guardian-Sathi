package com.biprangshu.guardiansathi.Guardian.data

data class GuardianAlert(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val desc: String = "",
    val imp: String = "LOW",
    val timestamp: Long = 0L,
    val appName: String = ""
)
