package fr.sutoko.inapppurchase.billing

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlayBillingCatalog @Inject constructor() : BillingCatalog {

    override suspend fun getProduct(productId: String): BillingProduct {
        val kind = when {
            productId.contains("removeads") -> ProductKind.NON_CONSUMABLE
            productId.contains("premium") -> ProductKind.SUBSCRIPTION
            productId.contains("story") -> ProductKind.NON_CONSUMABLE
            productId.contains("coins_pack") -> ProductKind.CONSUMABLE
            else -> ProductKind.CONSUMABLE
        }
        return BillingProduct(
            sku = productId,
            kind = kind,
        )
    }

    override suspend fun getProducts(productIds: List<String>): List<BillingProduct> {
        return productIds.map { productId ->
            getProduct(productId)
        }
    }
}
