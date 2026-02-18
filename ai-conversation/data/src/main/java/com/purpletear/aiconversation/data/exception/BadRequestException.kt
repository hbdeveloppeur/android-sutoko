package com.purpletear.aiconversation.data.exception

class BadRequestException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)