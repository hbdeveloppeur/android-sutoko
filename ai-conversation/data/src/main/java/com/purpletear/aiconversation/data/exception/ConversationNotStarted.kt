package com.purpletear.aiconversation.data.exception

class ConversationNotStarted(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)