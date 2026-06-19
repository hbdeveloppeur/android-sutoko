package com.purpletear.platform.play.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import org.json.JSONArray
import org.json.JSONObject

internal fun productDetails(json: String): ProductDetails {
    val constructor = ProductDetails::class.java.getDeclaredConstructor(String::class.java)
    constructor.isAccessible = true
    return constructor.newInstance(json)
}

internal fun inAppProductDetails(
    sku: String,
    title: String = sku,
    description: String = sku,
    formattedPrice: String = "$1.00",
): ProductDetails = productDetails(
    """
    {
      "productId": "$sku",
      "type": "inapp",
      "title": "$title",
      "name": "$title",
      "description": "$description",
      "oneTimePurchaseOfferDetails": {
        "formattedPrice": "$formattedPrice",
        "priceAmountMicros": 1000000,
        "priceCurrencyCode": "USD"
      }
    }
    """.trimIndent()
)

internal fun subscriptionProductDetails(
    sku: String,
    offerToken: String,
    title: String = sku,
    description: String = sku,
    formattedPrice: String = "$5.00",
): ProductDetails = productDetails(
    """
    {
      "productId": "$sku",
      "type": "subs",
      "title": "$title",
      "name": "$title",
      "description": "$description",
      "subscriptionOfferDetails": [
        {
          "offerIdToken": "$offerToken",
          "basePlanId": "base",
          "pricingPhases": [
            {
              "formattedPrice": "$formattedPrice",
              "priceAmountMicros": 5000000,
              "priceCurrencyCode": "USD",
              "billingPeriod": "P1M",
              "billingCycleCount": 0,
              "recurrenceMode": 1
            }
          ]
        }
      ]
    }
    """.trimIndent()
)

internal fun purchase(
    productIds: List<String>,
    token: String,
    purchaseState: Int = Purchase.PurchaseState.PURCHASED,
    acknowledged: Boolean = false,
    orderId: String? = "order-$token",
    purchaseTime: Long = 12345678L,
    signature: String = "",
): Purchase {
    val json = JSONObject().apply {
        put("productIds", JSONArray(productIds))
        put("purchaseToken", token)
        put(
            "purchaseState",
            when (purchaseState) {
                Purchase.PurchaseState.PURCHASED -> 1
                Purchase.PurchaseState.PENDING -> 4
                else -> purchaseState
            },
        )
        put("acknowledged", acknowledged)
        put("purchaseTime", purchaseTime)
        orderId?.let { put("orderId", it) }
        put("packageName", "com.test")
    }
    return Purchase(json.toString(), signature)
}
