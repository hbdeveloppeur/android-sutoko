package fr.sutoko.inapppurchase.billing

internal interface BillingCatalog {
    suspend fun getProduct(productId: String): BillingProduct
    suspend fun getProducts(productIds: List<String>): List<BillingProduct>
}