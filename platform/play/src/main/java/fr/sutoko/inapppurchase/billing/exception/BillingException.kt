package fr.sutoko.inapppurchase.billing.exception

class BillingException(
    val responseCode: Int,
    message: String,
) : RuntimeException(message)