package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.core.domain.User
import com.biprangshu.guardiansathi.Global.data.UserSessionManager
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
}