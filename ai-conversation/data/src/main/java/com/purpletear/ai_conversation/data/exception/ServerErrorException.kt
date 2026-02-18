package com.purpletear.ai_conversation.data.exception

class ServerErrorException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)