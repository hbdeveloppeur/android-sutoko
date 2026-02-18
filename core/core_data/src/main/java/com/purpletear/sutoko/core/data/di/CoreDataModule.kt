package com.purpletear.sutoko.core.data.di

import com.purpletear.sutoko.core.data.provider.HostProviderImpl
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing Core data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {

    /**
     * Provides the HostProvider implementation.
     *
     * @return The HostProvider implementation.
     */
    @Provides
    @Singleton
    fun provideHostProvider(): HostProvider {
        return HostProviderImpl("https://dashboard.sutoko.app/api/")
    }
}