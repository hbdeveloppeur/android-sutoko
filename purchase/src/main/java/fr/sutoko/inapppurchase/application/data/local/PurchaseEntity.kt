package fr.sutoko.inapppurchase.application.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import androidx.annotation.Keep

@Entity(tableName = "purchases")
@Keep
data class PurchaseEntity(
    @PrimaryKey
    val sku: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val acknowledged: Boolean,
    val purchaseState: Int = PurchaseState.PURCHASED,
    val orderId: String? = null,
    val backendRegistered: Boolean = false,
)
