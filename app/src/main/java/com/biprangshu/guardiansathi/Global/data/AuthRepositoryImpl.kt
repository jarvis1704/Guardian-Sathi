package com.biprangshu.guardiansathi.Global.core.data

import android.util.Log
import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.core.domain.User
import com.biprangshu.guardiansathi.Global.data.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val userSessionManager: UserSessionManager
) : AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Result<User, DataError.Network> {
        val authResult = firebaseAuthDataSource.signInWithGoogle(idToken)
        val userRole = userSessionManager.userRole.firstOrNull()
        
        return when (authResult) {
            is Result.Success -> {
                val firebaseUser = authResult.data
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    role = userRole
                )
                fetchAndSaveToken()
                firestoreUserDataSource.saveOrUpdateUser(user)
            }
            is Result.Error -> {
                Result.Error(authResult.error)
            }
        }
    }

    override suspend fun updateUserRole(role: String): Result<Unit, DataError.Network> {
        val uid = firebaseAuthDataSource.getCurentUserUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return firestoreUserDataSource.updateUserRole(uid, role)
    }

    override fun fetchAndSaveToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Current token: $token")
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                FirebaseDatabase.getInstance().reference
                    .child(uid)
                    .child("device_token")
                    .setValue(token)
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to get token: ${e.message}")
            }
    }

    override suspend fun signOut(): Result<Unit, DataError.Network> {
        return try {
            firebaseAuthDataSource.signOut()
            userSessionManager.setLoggedIn(false)
            userSessionManager.setLinked(false)
            userSessionManager.setUserRole(null)
            userSessionManager.setGuardianInfo(null, null)
            userSessionManager.setElderInfo(null, null)
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}