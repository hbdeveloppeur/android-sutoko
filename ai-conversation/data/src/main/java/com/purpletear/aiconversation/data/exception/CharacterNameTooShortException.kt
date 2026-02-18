package com.purpletear.aiconversation.data.exception

class CharacterNameTooShortException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)