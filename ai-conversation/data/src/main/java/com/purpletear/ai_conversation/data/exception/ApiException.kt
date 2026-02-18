package com.purpletear.ai_conversation.data.exception

open class ApiException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)