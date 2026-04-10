package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.LinkRepository
import com.biprangshu.guardiansathi.Global.core.domain.LinkStatus
import com.biprangshu.guardiansathi.Global.core.domain.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LinkRepositoryImpl @Inject constructor(
    private val firestoreLinkDataSource: FirestoreLinkDataSource
) : LinkRepository {

    override suspend fun generateLinkCode(elderUid: String): Result<String, DataError.Network> =
        firestoreLinkDataSource.generateLinkCode(elderUid)

    override suspend fun linkViaCode(
        code: String,
        guardianUid: String
    ): Result<Unit, DataError.Network> {
        val linkCodeResult = firestoreLinkDataSource.getLinkCodeDocument(code)
        if (linkCodeResult is Result.Error) return Result.Error(linkCodeResult.error)
        val linkCode = (linkCodeResult as Result.Success).data
        return firestoreLinkDataSource.linkUsers(guardianUid, linkCode.elderUid, code)
    }

    override fun observeLinkStatus(uid: String): Flow<LinkStatus> =
        firestoreLinkDataSource.observeLinkStatus(uid)

    override suspend fun getLinkStatus(uid: String): Result<LinkStatus, DataError.Network> =
        firestoreLinkDataSource.getLinkStatus(uid)

    override suspend fun deleteLinkCode(code: String): Result<Unit, DataError.Network> =
        firestoreLinkDataSource.deleteLinkCode(code)
}
