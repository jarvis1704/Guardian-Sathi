package com.biprangshu.guardiansathi.Elder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GuardianContact::class], version = 1, exportSchema = false)
abstract class GuardianSaathiDatabase: RoomDatabase() {
    abstract fun guardianContactDao(): GuardianContactDao
    abstract fun elderNotificationDao(): ElderNotificationsDao
}