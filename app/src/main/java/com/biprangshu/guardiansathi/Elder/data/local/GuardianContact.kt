package com.biprangshu.guardiansathi.Elder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guardian_contacts")
data class GuardianContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String
)
