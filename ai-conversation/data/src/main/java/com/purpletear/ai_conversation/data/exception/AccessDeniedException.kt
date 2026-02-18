package com.purpletear.ai_conversation.data.exception

class AccessDeniedException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)