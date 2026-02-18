package com.purpletear.shop.data.exception

open class ItemAlreadyOwnedErrorException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)