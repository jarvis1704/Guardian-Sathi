package com.biprangshu.guardiansathi.Global.core.domain

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val role: String?
)