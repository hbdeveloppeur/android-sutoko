package fr.sutoko.in_app_purchase_domain.model

import androidx.annotation.Keep

@Keep
data class AppProductDetails(
    val productId: String,
    val price: String?
)