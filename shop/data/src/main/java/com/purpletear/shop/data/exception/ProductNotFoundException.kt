package com.purpletear.shop.data.exception

open class ProductNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)