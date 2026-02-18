package com.purpletear.shop.data.exception

open class TrialConsumedException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)