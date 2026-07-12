package com.purpletear.sutoko.shop.di

import com.purpletear.sutoko.shop.data.repository.InMemoryCoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoinPurchaseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoinPurchaseRepository(
        impl: InMemoryCoinPurchaseRepository
    ): CoinPurchaseRepository
}
