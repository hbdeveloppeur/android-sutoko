package com.purpletear.ai_conversation.data.exception

class UnknownActionException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)