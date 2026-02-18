package fr.sutoko.inapppurchase.data.utils

import com.android.billingclient.api.Purchase
import fr.sutoko.inapppurchase.domain.enums.AppPurchaseType
import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails

object AppPurchaseDetailFromGooglePlayBilling {

    /**
     * Executes the logic
     * @param purchase : Purchase
     * @return AppPurchaseDetails
     */
    fun execute(purchase: Purchase): AppPurchaseDetails {
        return AppPurchaseDetails(
            orderId = purchase.orderId,
            products = purchase.products.map {
                Pair(it, getPurchaseType(it))
            },
            state = purchase.purchaseState,
            purchaseToken = purchase.purchaseToken,
            isAcknowledged = purchase.isAcknowledged
        )
    }

    private fun getPurchaseType(identifier: String): AppPurchaseType {
        AppPurchaseType.entries.forEach {
            if (identifier.startsWith(it.prefix)) {
                return@getPurchaseType it
            }
        }
        return AppPurchaseType.UNKNOWN
    }
}