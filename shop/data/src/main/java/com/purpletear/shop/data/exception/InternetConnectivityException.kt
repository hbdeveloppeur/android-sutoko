package com.purpletear.shop.data.exception

/**
 * Exception thrown when there are internet connectivity issues.
 */
open class InternetConnectivityException(
    message: String? = "Internet connectivity issue detected",
    cause: Throwable? = null
) : ShopApiException(message, cause)