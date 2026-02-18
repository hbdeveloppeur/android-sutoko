package com.purpletear.shop.data.exception

open class MissingParametersException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)