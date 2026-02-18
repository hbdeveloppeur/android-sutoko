package com.purpletear.aiconversation.data.exception

class UserMessageCountNotFound(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)