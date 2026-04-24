package com.biprangshu.guardiansathi.Global.Elder.di

import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepositoryImpl
import com.biprangshu.guardiansathi.Global.Elder.data.ElderForegroundServiceRepository
import com.biprangshu.guardiansathi.Global.Elder.data.ElderForegroundServiceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ElderServiceModule {

    @Binds
    @Singleton
    abstract fun bindElderForegroundServiceRepository(
        elderForegroundServiceRepositoryImpl: ElderForegroundServiceRepositoryImpl
    ): ElderForegroundServiceRepository

    @Binds
    @Singleton
    abstract fun bindElderFirebaseRepository(
        elderFirebaseRepositoryImpl: ElderFirebaseRepositoryImpl
    ): ElderFirebaseRepository
}
