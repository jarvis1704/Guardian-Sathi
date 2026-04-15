package com.biprangshu.guardiansathi.Elder.di

import com.biprangshu.guardiansathi.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceAssistantModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",  // updated model name
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}