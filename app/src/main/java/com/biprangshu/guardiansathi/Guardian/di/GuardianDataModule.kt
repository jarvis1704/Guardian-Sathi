package com.biprangshu.guardiansathi.Guardian.di

import com.biprangshu.guardiansathi.Guardian.data.GuardianAlertsRepository
import com.biprangshu.guardiansathi.Guardian.data.GuardianAlertsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuardianDataModule {

    @Binds
    @Singleton
    abstract fun bindGuardianAlertsRepository(
        guardianAlertsRepositoryImpl: GuardianAlertsRepositoryImpl
    ): GuardianAlertsRepository
}
