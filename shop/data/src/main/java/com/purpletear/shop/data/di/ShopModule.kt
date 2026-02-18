package com.purpletear.shop.data.di

import android.content.SharedPreferences
import com.purpletear.shop.data.remote.CatalogApi
import com.purpletear.shop.data.repository.CatalogRepositoryImpl
import com.purpletear.shop.domain.repository.CatalogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing Shop data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ShopModule {

    /**
     * Provides an implementation of CatalogRepository.
     *
     * @param api The CatalogApi used to make network requests
     * @param sharedPreferences SharedPreferences for storing local data
     * @return An implementation of CatalogRepository
     */
    @Provides
    @Singleton
    fun provideCatalogRepository(
        api: CatalogApi,
        sharedPreferences: SharedPreferences,
    ): CatalogRepository {
        return CatalogRepositoryImpl(api, sharedPreferences)
    }
}
