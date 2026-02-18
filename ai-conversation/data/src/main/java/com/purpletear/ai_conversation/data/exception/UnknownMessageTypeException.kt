package com.purpletear.ai_conversation.data.exception

class UnknownMessageTypeException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)