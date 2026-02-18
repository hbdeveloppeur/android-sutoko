package com.purpletear.aiconversation.data.exception

class ModelNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)