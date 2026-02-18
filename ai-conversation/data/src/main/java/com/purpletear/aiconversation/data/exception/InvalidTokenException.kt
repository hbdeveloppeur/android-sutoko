package com.purpletear.aiconversation.data.exception

class InvalidTokenException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)