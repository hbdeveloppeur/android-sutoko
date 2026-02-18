package fr.sutoko.inapppurchase.data.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.sutoko.inapppurchase.data.data_service.BillingDataServiceImpl
import fr.sutoko.inapppurchase.data.datasource.BillingDataSource
import fr.sutoko.inapppurchase.data.repository.BillingRepositoryImpl
import fr.sutoko.inapppurchase.domain.data_service.BillingDataService
import fr.sutoko.inapppurchase.domain.repository.BillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InAppPurchaseModule {
 

    @Provides
    @Singleton
    fun provideBillingImplRepository(billingDataSource: BillingDataSource): BillingRepository {
        return BillingRepositoryImpl(
            billingClient = billingDataSource.client
        )
    }

    @Provides
    @Singleton
    fun provideBillingDataService(dataSource: BillingDataSource): BillingDataService {
        return BillingDataServiceImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideBillingDataSource(@ApplicationContext context: Context): BillingDataSource {
        return BillingDataSource(context)
    }
}