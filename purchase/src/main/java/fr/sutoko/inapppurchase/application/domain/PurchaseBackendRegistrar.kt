package fr.sutoko.inapppurchase.application.domain

interface PurchaseBackendRegistrar {
    suspend fun supports(sku: String): Boolean
    suspend fun register(sku: String, purchaseToken: String, orderId: String?): Result<Unit>
}
