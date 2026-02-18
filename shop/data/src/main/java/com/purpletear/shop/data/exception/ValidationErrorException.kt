package com.purpletear.shop.data.exception

open class ValidationErrorException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)