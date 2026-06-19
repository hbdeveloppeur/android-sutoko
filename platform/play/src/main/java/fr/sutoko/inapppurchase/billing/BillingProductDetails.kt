package fr.sutoko.inapppurchase.billing

import androidx.annotation.Keep

@Keep
data class BillingProductDetails(
    val sku: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
)
