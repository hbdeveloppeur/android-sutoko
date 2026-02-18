package com.purpletear.aiconversation.data.exception

class NotEnoughCoinsException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)