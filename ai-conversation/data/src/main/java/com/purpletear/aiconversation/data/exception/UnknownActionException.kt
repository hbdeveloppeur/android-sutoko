package com.purpletear.aiconversation.data.exception

class UnknownActionException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)