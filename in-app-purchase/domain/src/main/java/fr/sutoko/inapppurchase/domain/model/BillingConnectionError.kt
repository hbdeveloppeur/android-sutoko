package fr.sutoko.inapppurchase.domain.model

/**
 * Represents errors that can occur when connecting to Google Play Billing.
 */
sealed class BillingConnectionError : Exception() {
    /**
     * Error when the billing service is disconnected.
     */
    data object ServiceDisconnected : BillingConnectionError()

    /**
     * Error when the billing service is unavailable.
     */
    data object ServiceUnavailable : BillingConnectionError()

    /**
     * Error when there's a network issue.
     */
    data object NetworkError : BillingConnectionError()

    /**
     * Error when the feature is not supported.
     */
    data object FeatureNotSupported : BillingConnectionError()

    /**
     * Error when there's a developer error.
     */
    data object DeveloperError : BillingConnectionError()

    /**
     * Error when the billing is unavailable.
     */
    data object BillingUnavailable : BillingConnectionError()

    /**
     * Generic error with a message.
     */
    data class GenericError(override val message: String) : BillingConnectionError()
}
