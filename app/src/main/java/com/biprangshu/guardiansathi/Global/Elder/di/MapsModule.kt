package com.biprangshu.guardiansathi.Global.Elder.di

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.biprangshu.guardiansathi.BuildConfig
import com.biprangshu.guardiansathi.Global.Elder.data.GoogleLocationRepository
import com.biprangshu.guardiansathi.Global.Elder.data.GoogleLocationRepositoryImpl
import dagger.Binds

@Module
@InstallIn(SingletonComponent::class)
abstract class MapsModule {

    @Binds
    @Singleton
    abstract fun bindGoogleLocationRepository(
        impl: GoogleLocationRepositoryImpl
    ): GoogleLocationRepository

    companion object {

        @Provides
        @Singleton
        fun providePlacesClient(
            @ApplicationContext context: Context
        ): PlacesClient {
            if (!Places.isInitialized()) {
                Places.initialize(context, BuildConfig.MAPS_API_KEY)
            }
            return Places.createClient(context)
        }
    }
}