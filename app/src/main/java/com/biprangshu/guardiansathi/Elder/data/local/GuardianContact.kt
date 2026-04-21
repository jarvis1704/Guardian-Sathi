package com.biprangshu.guardiansathi.Elder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "guardian_contacts")
data class GuardianContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String
)

@Dao
interface GuardianContactDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(contact: GuardianContact)

    @Delete
    suspend fun delete(contact: GuardianContact)

    @Query("SELECT * FROM guardian_contacts")
    fun getAllContacts(): Flow<List<GuardianContact>>
}