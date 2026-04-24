package com.biprangshu.guardiansathi.Global.core.domain

import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    suspend fun addReminder(elderUid: String, reminder: MedicineReminder): Result<Unit, DataError.Network>
    suspend fun updateReminder(elderUid: String, reminder: MedicineReminder): Result<Unit, DataError.Network>
    suspend fun deleteReminder(elderUid: String, reminderId: String): Result<Unit, DataError.Network>
    fun getReminders(elderUid: String): Flow<List<MedicineReminder>>
    suspend fun markAsTaken(elderUid: String, reminder: MedicineReminder, status: MedicineStatus): Result<Unit, DataError.Network>
}
