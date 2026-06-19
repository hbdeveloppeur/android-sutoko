package fr.sutoko.inapppurchase.billing

import androidx.annotation.Keep

sealed interface PurchaseResult {
    val sku: String?

    @Keep
    data class Purchased(
        val receipt: PurchaseReceipt,
    ) : PurchaseResult {
        override val sku: String = receipt.sku
    }

    @Keep
    data class Pending(
        val receipt: PurchaseReceipt,
    ) : PurchaseResult {
        override val sku: String = receipt.sku
    }

    @Keep
    data class AlreadyOwned(
        override val sku: String,
    ) : PurchaseResult

    data object Canceled : PurchaseResult {
        override val sku: String? = null
    }

    @Keep
    data class Failed(
        override val sku: String?,
        val responseCode: Int? = null,
        val message: String,
        val cause: Throwable? = null,
    ) : PurchaseResult
}