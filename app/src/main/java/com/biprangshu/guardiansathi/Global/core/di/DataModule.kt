package com.biprangshu.guardiansathi.Global.core.di

import android.content.Context
import com.biprangshu.guardiansathi.Global.core.data.RoomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideRoomRepository(
        @ApplicationContext context: Context
    ): RoomRepository {
        return RoomRepository(context)
    }
}
