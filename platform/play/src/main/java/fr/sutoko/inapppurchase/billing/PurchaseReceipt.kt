package fr.sutoko.inapppurchase.billing

import com.android.billingclient.api.Purchase
import androidx.annotation.Keep

@Keep
data class PurchaseReceipt(
    val sku: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val acknowledged: Boolean,
    val purchaseState: Int,
    val orderId: String? = null,
) {
    val isPurchased: Boolean
        get() = purchaseState == Purchase.PurchaseState.PURCHASED

    val isPending: Boolean
        get() = purchaseState == Purchase.PurchaseState.PENDING
}