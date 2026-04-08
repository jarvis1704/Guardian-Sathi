package com.biprangshu.guardiansathi.Global.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

//    @Provides
//    @Singleton
//    fun provideRoomRepository(
//        @ApplicationContext context: Context
//    ): RoomRepository {
//        return RoomRepository(context)
//    }
}
