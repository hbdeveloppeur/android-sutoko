package fr.sutoko.inapppurchase.application.domain

/**
 * Thrown by [PurchaseBackendRegistrar] implementations when the backend definitively
 * rejects a purchase registration (HTTP 4xx: invalid/revoked token, unknown purchase).
 *
 * The coordinator purges the local purchase state instead of retrying, so the UI
 * no longer treats the purchase as owned.
 */
class PurchaseRegistrationRejectedException(message: String) : Exception(message)
