package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.core.domain.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Result<User, DataError.Network> {
        val authResult = firebaseAuthDataSource.signInWithGoogle(idToken)
        
        return when (authResult) {
            is Result.Success -> {
                val firebaseUser = authResult.data
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                firestoreUserDataSource.saveOrUpdateUser(user)
            }
            is Result.Error -> {
                Result.Error(authResult.error)
            }
        }
    }
}