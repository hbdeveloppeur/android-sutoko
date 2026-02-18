package fr.sutoko.inapppurchase.domain.data_service

import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.StateFlow

interface BillingDataService {

    fun getPurchases(): StateFlow<List<AppPurchaseDetails>>
}