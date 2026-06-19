package fr.sutoko.inapppurchase.billing.exception

class PurchaseAlreadyOwnedException(
    val sku: String
) : Exception("Purchase was cancelled for SKU: $sku")