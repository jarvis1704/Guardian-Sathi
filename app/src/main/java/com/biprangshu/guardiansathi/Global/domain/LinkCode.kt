package com.biprangshu.guardiansathi.Global.core.domain

import com.google.firebase.Timestamp

data class LinkCode(
    val code: String,
    val elderUid: String,
    val createdAt: Timestamp,
    val expiresAt: Timestamp
)
