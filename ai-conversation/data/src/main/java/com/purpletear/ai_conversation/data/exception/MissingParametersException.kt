package com.purpletear.ai_conversation.data.exception

class MissingParametersException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)