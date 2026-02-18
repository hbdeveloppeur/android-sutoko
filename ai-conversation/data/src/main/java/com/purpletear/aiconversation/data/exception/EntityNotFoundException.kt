package com.purpletear.aiconversation.data.exception

class EntityNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)