package com.purpletear.game.data.di

import com.purpletear.game.data.repository.Zip4jRepositoryImpl
import com.purpletear.sutoko.game.repository.ZipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger/Hilt module for providing zip-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ZipModule {
    
    /**
     * Binds the [Zip4jRepositoryImpl] implementation to the [ZipRepository] interface.
     *
     * @param impl The implementation of [ZipRepository].
     * @return The bound [ZipRepository] instance.
     */
    @Binds
    @Singleton
    abstract fun bindZipRepository(impl: Zip4jRepositoryImpl): ZipRepository
}