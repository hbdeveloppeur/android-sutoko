package fr.sutoko.in_app_purchase_domain.data_service

import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.StateFlow

interface BillingDataService {

    fun getPurchases(): StateFlow<List<AppPurchaseDetails>>
}