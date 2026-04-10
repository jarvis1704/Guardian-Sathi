package com.biprangshu.guardiansathi.Global.core.di

import com.biprangshu.guardiansathi.Global.core.data.LinkRepositoryImpl
import com.biprangshu.guardiansathi.Global.core.domain.LinkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LinkModule {

    @Binds
    @Singleton
    abstract fun bindLinkRepository(
        impl: LinkRepositoryImpl
    ): LinkRepository
}
