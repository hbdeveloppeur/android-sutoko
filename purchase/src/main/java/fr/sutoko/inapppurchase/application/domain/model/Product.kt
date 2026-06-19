package fr.sutoko.inapppurchase.application.domain.model

import androidx.annotation.Keep

@Keep
data class Product(
    val sku: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
)
