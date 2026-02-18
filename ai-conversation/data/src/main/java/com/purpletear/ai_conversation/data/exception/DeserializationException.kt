package com.purpletear.ai_conversation.data.exception

class DeserializationException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)