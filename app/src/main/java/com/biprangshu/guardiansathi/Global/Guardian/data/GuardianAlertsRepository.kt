package com.biprangshu.guardiansathi.Global.Guardian.data

import kotlinx.coroutines.flow.Flow

interface GuardianAlertsRepository {
    fun getAlerts(guardianUid: String): Flow<List<GuardianAlert>>
}
