package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser, DataError.Network> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNAUTHORIZED)
        }
    }

    fun getCurentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    fun getCurrentUserPhotoUrl(): String? {
        return firebaseAuth.currentUser?.photoUrl?.toString()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}