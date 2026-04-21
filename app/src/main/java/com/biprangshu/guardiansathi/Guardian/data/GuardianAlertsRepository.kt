package com.biprangshu.guardiansathi.Guardian.data

import kotlinx.coroutines.flow.Flow

interface GuardianAlertsRepository {
    fun getAlerts(guardianUid: String): Flow<List<GuardianAlert>>
}
