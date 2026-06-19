package fr.sutoko.inapppurchase.application.domain.model

/**
 * Platform-agnostic purchase-state values.
 *
 * These intentionally match Play Billing's Purchase.PurchaseState constants
 * so the platform layer can pass raw integers through without translation.
 */
object PurchaseState {
    const val PURCHASED = 1
    const val PENDING = 2
}
