package com.purpletear.ai_conversation.data.exception

class InsufficientFundsException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)