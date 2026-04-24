package com.biprangshu.guardiansathi.Global.core.domain

import java.util.UUID

data class MedicineReminder(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val dosage: String = "", // e.g., "1 pill", "5ml"
    val times: List<MedicineTime> = emptyList(), // List of times in a day
    val daysOfWeek: List<Int> = (1..7).toList(), // 1=Sunday, 7=Saturday
    val isActive: Boolean = true,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val instructions: String? = null // e.g., "After food"
)

data class MedicineTime(
    val hour: Int = 0,
    val minute: Int = 0
)

data class MedicineLog(
    val id: String = UUID.randomUUID().toString(),
    val reminderId: String = "",
    val medicineName: String = "",
    val status: MedicineStatus = MedicineStatus.MISSED,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MedicineStatus {
    TAKEN,
    MISSED,
    SKIPPED
}
