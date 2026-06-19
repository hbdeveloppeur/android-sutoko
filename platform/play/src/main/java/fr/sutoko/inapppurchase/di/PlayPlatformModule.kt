package fr.sutoko.inapppurchase.di

import fr.sutoko.inapppurchase.billing.BillingCatalog
import fr.sutoko.inapppurchase.billing.BillingClientWrapperFactory
import fr.sutoko.inapppurchase.billing.BillingDataSource
import fr.sutoko.inapppurchase.billing.NetworkPurchaseVerifier
import fr.sutoko.inapppurchase.billing.PlayBillingCatalog
import fr.sutoko.inapppurchase.billing.PlayBillingClientWrapperFactory
import fr.sutoko.inapppurchase.billing.PlayBillingDataSource
import fr.sutoko.inapppurchase.billing.PurchaseVerifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlayPlatformModule {

    @Binds
    abstract fun bindBillingDataSource(impl: PlayBillingDataSource): BillingDataSource

    @Binds
    abstract fun bindBillingClientWrapperFactory(impl: PlayBillingClientWrapperFactory): BillingClientWrapperFactory

    @Binds
    abstract fun bindBillingCatalog(impl: PlayBillingCatalog): BillingCatalog

    @Binds
    abstract fun bindPurchaseVerifier(impl: NetworkPurchaseVerifier): PurchaseVerifier
}
