package com.purpletear.shop.data.exception

open class InvalidTokenException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)