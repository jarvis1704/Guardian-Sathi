package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.core.domain.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveOrUpdateUser(user: User): Result<User, DataError.Network> {
        return try {
            val userData = mapOf(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "photoUrl" to user.photoUrl
            )
            // Use set with merge to create or update the user document without overwriting other fields if they exist
            usersCollection.document(user.uid).set(userData, SetOptions.merge()).await()
            
            // After saving, fetch the latest data to return
            val document = usersCollection.document(user.uid).get().await()
            if (document.exists()) {
                val fetchedUser = User(
                    uid = document.getString("uid") ?: user.uid,
                    email = document.getString("email"),
                    displayName = document.getString("displayName"),
                    photoUrl = document.getString("photoUrl")
                )
                Result.Success(fetchedUser)
            } else {
                Result.Error(DataError.Network.NOT_FOUND)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}