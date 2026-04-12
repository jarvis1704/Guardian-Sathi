package com.biprangshu.guardiansathi.Elder.di

import com.biprangshu.guardiansathi.Elder.data.PermissionManagerRepository
import com.biprangshu.guardiansathi.Elder.data.PermissionManagerRepositoryImpl
import dagger.Binds
import dagger.Module
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