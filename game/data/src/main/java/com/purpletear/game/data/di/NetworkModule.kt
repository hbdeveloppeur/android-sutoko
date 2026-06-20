package com.purpletear.game.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = "https://sutoko.com/"

    @Provides
    @Named("mediaBaseUrl")
    fun provideMediaBaseUrl(): String = "https://sutoko.com/media/"
}