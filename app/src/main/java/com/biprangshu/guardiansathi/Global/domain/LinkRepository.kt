package com.biprangshu.guardiansathi.Global.core.domain

import kotlinx.coroutines.flow.Flow

interface LinkRepository {

    /** Elder: generates a "SAATHI-XXXXXX" code in Firestore, returns the code string. */
    suspend fun generateLinkCode(elderUid: String): Result<String, DataError.Network>

    /** Guardian: validates the code, then atomically links both user documents. */
    suspend fun linkViaCode(
        code: String,
        guardianUid: String
    ): Result<Unit, DataError.Network>

    /** Real-time listener on users/{uid} — auto-navigates Elder when Guardian links. */
    fun observeLinkStatus(uid: String): Flow<LinkStatus>

    /** One-shot Firestore read — used by SplashViewModel to verify link status. */
    suspend fun getLinkStatus(uid: String): Result<LinkStatus, DataError.Network>

    /** Deletes a stale linkCode when Elder leaves the screen without being linked. */
    suspend fun deleteLinkCode(code: String): Result<Unit, DataError.Network>
}
