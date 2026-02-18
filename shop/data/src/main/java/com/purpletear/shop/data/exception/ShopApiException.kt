package com.purpletear.shop.data.exception

open class ShopApiException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)