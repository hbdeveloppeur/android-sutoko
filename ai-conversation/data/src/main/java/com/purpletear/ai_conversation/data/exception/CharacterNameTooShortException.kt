package com.purpletear.ai_conversation.data.exception

class CharacterNameTooShortException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)