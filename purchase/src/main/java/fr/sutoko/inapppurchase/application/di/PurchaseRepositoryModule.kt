package fr.sutoko.inapppurchase.application.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseRepositoryModule {

    @Binds
    abstract fun bindPurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository
}
