package com.biprangshu.guardiansathi.Global.core.domain

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User, DataError.Network>
}