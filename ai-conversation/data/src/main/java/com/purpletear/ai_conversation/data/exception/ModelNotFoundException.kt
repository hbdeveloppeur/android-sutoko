package com.purpletear.ai_conversation.data.exception

class ModelNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)