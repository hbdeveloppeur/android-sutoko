package com.purpletear.ai_conversation.data.exception

class MalformedResponseException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)