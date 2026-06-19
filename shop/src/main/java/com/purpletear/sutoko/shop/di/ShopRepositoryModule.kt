package com.purpletear.sutoko.shop.di

import com.purpletear.sutoko.shop.data.repository.InMemoryShopRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShopRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShopRepository(
        impl: InMemoryShopRepository
    ): ShopRepository
}
