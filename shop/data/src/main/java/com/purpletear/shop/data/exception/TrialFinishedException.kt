package com.purpletear.shop.data.exception

open class TrialFinishedException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)