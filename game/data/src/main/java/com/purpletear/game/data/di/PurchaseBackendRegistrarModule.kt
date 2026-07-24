package com.purpletear.game.data.di

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
    abstract fun bindGamePurchaseBackendRegistrar(
        impl: GamePurchaseBackendRegistrar
    ): PurchaseBackendRegistrar
}
