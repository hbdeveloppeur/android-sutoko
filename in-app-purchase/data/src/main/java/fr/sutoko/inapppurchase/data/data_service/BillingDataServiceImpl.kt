package fr.sutoko.inapppurchase.data.data_service

import fr.sutoko.inapppurchase.data.datasource.BillingDataSource
import fr.sutoko.inapppurchase.domain.data_service.BillingDataService
import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BillingDataServiceImpl @Inject constructor(private val billingDataSource: BillingDataSource) :
    BillingDataService {

    override fun getPurchases(): StateFlow<List<AppPurchaseDetails>> {
        return billingDataSource.purchases
    }
}