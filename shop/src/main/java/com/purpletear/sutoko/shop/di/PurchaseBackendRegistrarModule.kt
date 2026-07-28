package com.purpletear.sutoko.shop.di

import com.purpletear.sutoko.shop.data.registrar.CoinsPackPurchaseBackendRegistrar
import com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseBackendRegistrarModule {

    @Binds
    @IntoSet
    abstract fun bindStoryPurchaseBackendRegistrar(
        impl: StoryPurchaseBackendRegistrar
    ): PurchaseBackendRegistrar

    @Binds
    @IntoSet
    abstract fun bindCoinsPackPurchaseBackendRegistrar(
        impl: CoinsPackPurchaseBackendRegistrar
    ): PurchaseBackendRegistrar
}
