package com.purpletear.platform.play.billing

import fr.sutoko.inapppurchase.billing.BillingCatalog
import fr.sutoko.inapppurchase.billing.BillingProduct

internal class FakeBillingCatalog : BillingCatalog {

    private val products = mutableMapOf<String, BillingProduct>()

    var getProductError: Throwable? = null

    fun add(product: BillingProduct) {
        products[product.sku] = product
    }

    override suspend fun getProduct(productId: String): BillingProduct {
        getProductError?.let { throw it }
        return products.getValue(productId)
    }

    override suspend fun getProducts(productIds: List<String>): List<BillingProduct> =
        productIds.map { getProduct(it) }
}
