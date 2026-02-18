package fr.sutoko.in_app_purchase_data.data_service

import fr.sutoko.in_app_purchase_data.datasource.BillingDataSource
import fr.sutoko.in_app_purchase_domain.data_service.BillingDataService
import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BillingDataServiceImpl @Inject constructor(private val billingDataSource: BillingDataSource) :
    BillingDataService {

    override fun getPurchases(): StateFlow<List<AppPurchaseDetails>> {
        return billingDataSource.purchases
    }
}