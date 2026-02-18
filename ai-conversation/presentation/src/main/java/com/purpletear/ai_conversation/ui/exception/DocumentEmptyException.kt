package com.purpletear.ai_conversation.ui.exception

class DocumentEmptyException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)