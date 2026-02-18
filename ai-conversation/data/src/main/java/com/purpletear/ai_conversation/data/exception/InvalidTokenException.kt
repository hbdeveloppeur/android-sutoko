package com.purpletear.ai_conversation.data.exception

class InvalidTokenException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)