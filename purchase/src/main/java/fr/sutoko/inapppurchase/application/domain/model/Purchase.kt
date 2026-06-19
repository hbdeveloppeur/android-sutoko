package fr.sutoko.inapppurchase.application.domain.model

import androidx.annotation.Keep

@Keep
data class Purchase(
    val sku: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val acknowledged: Boolean,
    val purchaseState: Int,
    val orderId: String?,
    val backendRegistered: Boolean,
)
