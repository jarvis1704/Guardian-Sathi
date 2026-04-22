package com.biprangshu.guardiansathi.Elder.di

import android.content.Context
import androidx.room.Room
import com.biprangshu.guardiansathi.Elder.data.local.ElderNotificationsDao
import com.biprangshu.guardiansathi.Elder.data.local.GuardianContactDao
import com.biprangshu.guardiansathi.Elder.data.local.GuardianSaathiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GuardianSaathiDatabase {
        return Room.databaseBuilder(
            context,
            GuardianSaathiDatabase::class.java,
            "guardian_saathi_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGuardianContactDao(db: GuardianSaathiDatabase): GuardianContactDao {
        return db.guardianContactDao()
    }

    @Provides
    fun provideElderNotificationsDao(db: GuardianSaathiDatabase): ElderNotificationsDao {
        return db.elderNotificationDao()
    }
}