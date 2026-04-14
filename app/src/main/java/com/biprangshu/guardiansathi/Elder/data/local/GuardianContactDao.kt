package com.biprangshu.guardiansathi.Elder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: GuardianContact)

    @Delete
    suspend fun delete(contact: GuardianContact)

    @Query("SELECT * FROM guardian_contacts")
    fun getAllContacts(): Flow<List<GuardianContact>>
}