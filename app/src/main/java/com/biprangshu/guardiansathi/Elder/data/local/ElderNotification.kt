package com.biprangshu.guardiansathi.Elder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "elder_notifications")
data class ElderNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val desc: String,
    val imp: String,
    val appName: String,
    val time: Long
)

@Dao
interface ElderNotificationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ElderNotification)

    @Delete
    suspend fun deleteNotification(notification: ElderNotification)

    @Query("SELECT * FROM elder_notifications ORDER BY time DESC")
    fun getAllNotifications(): Flow<List<ElderNotification>>
}