package fr.sutoko.inapppurchase.billing.exception

class PurchaseFailedException(
    val sku: String,
    val responseCode: Int?,
    val debugMessage: String?
) : Exception("Purchase failed for SKU: $sku, code=$responseCode, message=$debugMessage")