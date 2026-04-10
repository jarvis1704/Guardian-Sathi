package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.LinkCode
import com.biprangshu.guardiansathi.Global.core.domain.LinkStatus
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirestoreLinkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val linkCodesCollection = firestore.collection("linkCodes")

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val suffix = (1..6).map { chars.random() }.joinToString("")
        return "SAATHI-$suffix"
    }

    suspend fun generateLinkCode(elderUid: String): Result<String, DataError.Network> {
        return try {
            val code = generateCode()
            val now = Timestamp.now()
            val expiresAt = Timestamp(Date(now.toDate().time + 24L * 60 * 60 * 1000))
            val data = mapOf(
                "elderUid" to elderUid,
                "createdAt" to now,
                "expiresAt" to expiresAt
            )
            linkCodesCollection.document(code).set(data).await()
            Result.Success(code)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    suspend fun getLinkCodeDocument(code: String): Result<LinkCode, DataError.Network> {
        return try {
            val doc = linkCodesCollection.document(code).get().await()
            if (!doc.exists()) return Result.Error(DataError.Network.NOT_FOUND)
            val elderUid = doc.getString("elderUid")
                ?: return Result.Error(DataError.Network.UNKNOWN)
            val createdAt = doc.getTimestamp("createdAt")
                ?: return Result.Error(DataError.Network.UNKNOWN)
            val expiresAt = doc.getTimestamp("expiresAt")
                ?: return Result.Error(DataError.Network.UNKNOWN)
            if (expiresAt.toDate().before(Date())) {
                return Result.Error(DataError.Network.REQUEST_TIMEOUT)
            }
            Result.Success(LinkCode(code, elderUid, createdAt, expiresAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    /** Atomically updates both user documents and deletes the linkCode in one batch. */
    suspend fun linkUsers(
        guardianUid: String,
        elderUid: String,
        code: String
    ): Result<Unit, DataError.Network> {
        return try {
            firestore.runBatch { batch ->
                batch.update(
                    usersCollection.document(guardianUid),
                    mapOf("isLinked" to true, "linkedUid" to elderUid)
                )
                batch.update(
                    usersCollection.document(elderUid),
                    mapOf("isLinked" to true, "linkedUid" to guardianUid)
                )
                batch.delete(linkCodesCollection.document(code))
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    suspend fun getLinkStatus(uid: String): Result<LinkStatus, DataError.Network> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (!doc.exists()) return Result.Error(DataError.Network.NOT_FOUND)
            val isLinked = doc.getBoolean("isLinked") ?: false
            val linkedUid = doc.getString("linkedUid")
            Result.Success(LinkStatus(isLinked, linkedUid))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    fun observeLinkStatus(uid: String): Flow<LinkStatus> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val isLinked = snapshot.getBoolean("isLinked") ?: false
                val linkedUid = snapshot.getString("linkedUid")
                trySend(LinkStatus(isLinked, linkedUid))
            }
        awaitClose { registration?.remove() }
    }

    suspend fun deleteLinkCode(code: String): Result<Unit, DataError.Network> {
        return try {
            linkCodesCollection.document(code).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
