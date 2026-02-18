package com.purpletear.aiconversation.data.exception

class UserNameNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)