package com.purpletear.shop.data.exception

open class AccessDeniedException(
    message: String? = null,
    cause: Throwable? = null
) : ShopApiException(message, cause)