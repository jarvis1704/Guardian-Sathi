package com.biprangshu.guardiansathi.Elder.di

import com.biprangshu.guardiansathi.Elder.data.local.ContactRepository
import com.biprangshu.guardiansathi.Elder.data.local.ContactRepositoryImpl
import com.biprangshu.guardiansathi.Elder.data.local.ElderNotificationRepository
import com.biprangshu.guardiansathi.Elder.data.local.ElderNotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoomDatabaseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindElderNotificationsRepository(
        impl: ElderNotificationRepositoryImpl
    ): ElderNotificationRepository


}