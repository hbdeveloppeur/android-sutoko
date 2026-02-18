package com.purpletear.ai_conversation.ui.exception

class EmptyPromptFoundException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)