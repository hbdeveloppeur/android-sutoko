package fr.sutoko.inapppurchase.billing.exception

class PurchaseCancelledException(
    val sku: String
) : Exception("Purchase was cancelled for SKU: $sku")