package com.purpletear.shop.data.exception

open class AlreadyConsumedException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)