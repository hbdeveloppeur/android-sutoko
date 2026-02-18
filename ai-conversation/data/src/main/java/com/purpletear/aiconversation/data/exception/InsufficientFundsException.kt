package com.purpletear.aiconversation.data.exception

class InsufficientFundsException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)