package fr.sutoko.inapppurchase.billing

import com.android.billingclient.api.BillingClient

internal data class BillingProduct(
    val sku: String,
    val kind: ProductKind,

    /**
     * Required for subscriptions in most real Billing Library integrations.
     * Usually selected from ProductDetails.subscriptionOfferDetails.
     */
    val offerToken: String? = null,

    /**
     * Optional fraud/account correlation value.
     * Must not contain raw PII. Use a stable hashed user id.
     */
    val obfuscatedAccountId: String? = null,

    /**
     * Optional profile correlation value.
     * Must not contain raw PII.
     */
    val obfuscatedProfileId: String? = null,
) {
    val productType: String
        get() = when (kind) {
            ProductKind.CONSUMABLE,
            ProductKind.NON_CONSUMABLE -> BillingClient.ProductType.INAPP

            ProductKind.SUBSCRIPTION -> BillingClient.ProductType.SUBS
        }
}