package com.purpletear.purchase.data

import fr.sutoko.inapppurchase.application.data.local.PurchaseEntity
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.billing.BillingProductDetails
import fr.sutoko.inapppurchase.billing.PurchaseReceipt


object PurchaseTestFixtures {

    fun receipt(
        sku: String = "sku",
        purchaseToken: String = "token",
        purchaseTime: Long = 1_000,
        acknowledged: Boolean = true,
        purchaseState: Int = PurchaseState.PURCHASED,
        orderId: String? = "order-1",
    ): PurchaseReceipt = PurchaseReceipt(
        sku = sku,
        purchaseToken = purchaseToken,
        purchaseTime = purchaseTime,
        acknowledged = acknowledged,
        purchaseState = purchaseState,
        orderId = orderId,
    )

    fun entity(
        sku: String = "sku",
        purchaseToken: String = "token",
        purchaseTime: Long = 1_000,
        acknowledged: Boolean = true,
        purchaseState: Int = PurchaseState.PURCHASED,
        orderId: String? = "order-1",
        backendRegistered: Boolean = false,
    ): PurchaseEntity = PurchaseEntity(
        sku = sku,
        purchaseToken = purchaseToken,
        purchaseTime = purchaseTime,
        acknowledged = acknowledged,
        purchaseState = purchaseState,
        orderId = orderId,
        backendRegistered = backendRegistered,
    )

    fun productDetails(
        sku: String = "sku",
        title: String = "Title",
        description: String = "Description",
        formattedPrice: String = "$1.00",
    ): BillingProductDetails = BillingProductDetails(
        sku = sku,
        title = title,
        description = description,
        formattedPrice = formattedPrice,
    )
}
