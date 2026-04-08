package com.biprangshu.guardiansathi.Global.core.di

import com.biprangshu.guardiansathi.Global.core.data.UserSessionManager
import com.biprangshu.guardiansathi.Global.core.domain.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun bindSessionRepository(
        userSessionManager: UserSessionManager
    ): SessionRepository
}
