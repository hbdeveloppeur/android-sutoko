package fr.sutoko.inapppurchase.domain.model

import androidx.annotation.Keep
import com.android.billingclient.api.Purchase
import fr.sutoko.inapppurchase.domain.enums.AppPurchaseType

@Keep
data class AppPurchaseDetails(
    val orderId: String?,
    val products: List<Pair<String, AppPurchaseType>>,
    private val state: Int,
    val purchaseToken: String,
    val isAcknowledged: Boolean
) {
    fun isPurchased(): Boolean {
        return this.state == Purchase.PurchaseState.PURCHASED
    }

    fun isPending(): Boolean {
        return this.state == Purchase.PurchaseState.PENDING
    }
}

