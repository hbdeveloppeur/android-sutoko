package com.purpletear.aiconversation.data.exception

class AccessDeniedException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)