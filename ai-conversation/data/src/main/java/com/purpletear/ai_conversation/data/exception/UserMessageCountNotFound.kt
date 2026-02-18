package com.purpletear.ai_conversation.data.exception

class UserMessageCountNotFound(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)