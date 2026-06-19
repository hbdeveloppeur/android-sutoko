package fr.sutoko.inapppurchase.application.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.sutoko.inapppurchase.application.data.local.PurchaseDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PurchaseDatabaseModule {

    @Provides
    @Singleton
    fun providePurchaseDatabase(@ApplicationContext context: Context): PurchaseDatabase =
        PurchaseDatabase.create(context)

    @Provides
    fun providePurchaseDao(db: PurchaseDatabase) = db.purchaseDao()
}
