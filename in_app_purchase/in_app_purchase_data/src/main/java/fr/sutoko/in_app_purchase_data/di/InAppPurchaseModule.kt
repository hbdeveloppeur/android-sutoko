package fr.sutoko.in_app_purchase_data.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.sutoko.in_app_purchase_data.data_service.BillingDataServiceImpl
import fr.sutoko.in_app_purchase_data.datasource.BillingDataSource
import fr.sutoko.in_app_purchase_data.repository.BillingRepositoryImpl
import fr.sutoko.in_app_purchase_domain.data_service.BillingDataService
import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
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