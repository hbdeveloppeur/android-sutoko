package com.purpletear.ai_conversation.data.exception

class BadRequestException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)