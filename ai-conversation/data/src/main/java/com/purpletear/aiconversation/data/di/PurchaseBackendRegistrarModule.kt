package com.purpletear.aiconversation.data.di

import com.purpletear.aiconversation.data.registrar.AiMessagePackPurchaseBackendRegistrar
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
    abstract fun bindAiMessagePackPurchaseBackendRegistrar(
        impl: AiMessagePackPurchaseBackendRegistrar
    ): PurchaseBackendRegistrar
}
