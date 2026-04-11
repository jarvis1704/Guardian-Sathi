package com.biprangshu.guardiansathi.Global.di

import com.biprangshu.guardiansathi.Elder.data.PermissionManagerRepository
import com.biprangshu.guardiansathi.Elder.data.PermissionManagerRepositoryImpl
import com.google.android.datatransport.runtime.dagger.Binds
import com.google.android.datatransport.runtime.dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    @Singleton
    abstract fun bindPermissionManager(
        impl: PermissionManagerRepositoryImpl
    ): PermissionManagerRepository
}