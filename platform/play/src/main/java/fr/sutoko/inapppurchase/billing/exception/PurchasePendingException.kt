package fr.sutoko.inapppurchase.billing.exception

class PurchasePendingException(
    val sku: String
) : Exception("Purchase is pending for SKU: $sku")



