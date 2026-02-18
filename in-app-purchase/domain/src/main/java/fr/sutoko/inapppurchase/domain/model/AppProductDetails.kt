package fr.sutoko.inapppurchase.domain.model

import androidx.annotation.Keep

@Keep
data class AppProductDetails(
    val productId: String,
    val price: String?
)