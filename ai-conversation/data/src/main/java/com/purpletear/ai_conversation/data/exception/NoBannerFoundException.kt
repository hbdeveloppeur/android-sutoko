package com.purpletear.ai_conversation.data.exception

class NoBannerFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)