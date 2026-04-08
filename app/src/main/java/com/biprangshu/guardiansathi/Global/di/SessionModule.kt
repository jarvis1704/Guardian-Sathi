package com.biprangshu.guardiansathi.Global.di

import com.biprangshu.guardiansathi.Global.data.UserSessionManager
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
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
