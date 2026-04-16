package com.biprangshu.guardiansathi.Global.core.domain

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User, DataError.Network>
    suspend fun updateUserRole(role: String): Result<Unit, DataError.Network>
    fun fetchAndSaveToken()

    suspend fun signOut(): Result<Unit, DataError.Network>
}