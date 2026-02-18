package com.purpletear.ai_conversation.data.exception

class UnableToReachServiceException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)