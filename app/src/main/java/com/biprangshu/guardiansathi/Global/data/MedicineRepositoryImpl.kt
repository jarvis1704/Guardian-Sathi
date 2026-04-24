package com.biprangshu.guardiansathi.Global.core.data

import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.MedicineReminder
import com.biprangshu.guardiansathi.Global.core.domain.MedicineRepository
import com.biprangshu.guardiansathi.Global.core.domain.MedicineStatus
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Global.Elder.core.NotificationData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val elderFirebaseRepository: ElderFirebaseRepository
) : MedicineRepository {

    private fun getMedicineCollection(elderUid: String) =
        firestore.collection("users").document(elderUid).collection("medicineReminders")

    private fun getMedicineLogsCollection(elderUid: String) =
        firestore.collection("users").document(elderUid).collection("medicineLogs")

    override suspend fun addReminder(elderUid: String, reminder: MedicineReminder): Result<Unit, DataError.Network> {
        return try {
            getMedicineCollection(elderUid).document(reminder.id).set(reminder).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun updateReminder(elderUid: String, reminder: MedicineReminder): Result<Unit, DataError.Network> {
        return try {
            getMedicineCollection(elderUid).document(reminder.id).set(reminder).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun deleteReminder(elderUid: String, reminderId: String): Result<Unit, DataError.Network> {
        return try {
            getMedicineCollection(elderUid).document(reminderId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override fun getReminders(elderUid: String): Flow<List<MedicineReminder>> {
        return getMedicineCollection(elderUid).snapshots().map { snapshot ->
            snapshot.toObjects(MedicineReminder::class.java)
        }
    }

    override suspend fun markAsTaken(
        elderUid: String,
        reminder: MedicineReminder,
        status: MedicineStatus
    ): Result<Unit, DataError.Network> {
        return try {
            val log = mapOf(
                "reminderId" to reminder.id,
                "medicineName" to reminder.name,
                "status" to status.name,
                "timestamp" to System.currentTimeMillis()
            )
            getMedicineLogsCollection(elderUid).add(log).await()

            if (status == MedicineStatus.TAKEN) {
                val notifData = NotificationData(
                    packageName = "com.biprangshu.guardiansathi",
                    appName = "Guardian Saathi",
                    title = "Medicine Taken",
                    desc = "Elder has taken medicine: ${reminder.name}",
                    body = "Medicine: ${reminder.name} (${reminder.dosage}) taken.",
                    timestamp = System.currentTimeMillis()
                )
                elderFirebaseRepository.sendNotificaitonToGuardian(notifData, false, false, "LOW")
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
