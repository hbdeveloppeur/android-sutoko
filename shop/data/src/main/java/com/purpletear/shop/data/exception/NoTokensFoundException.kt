package com.purpletear.shop.data.exception

open class NoTokensFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)