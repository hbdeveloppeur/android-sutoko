package com.purpletear.ai_conversation.data.exception

class EntityNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)