package com.purpletear.aiconversation.presentation.exception

class EmptyPromptFoundException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)