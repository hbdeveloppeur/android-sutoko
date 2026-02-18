package com.purpletear.aiconversation.data.exception

class UnknownMessageTypeException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)