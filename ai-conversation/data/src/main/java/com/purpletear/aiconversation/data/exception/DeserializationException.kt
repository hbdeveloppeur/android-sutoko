package com.purpletear.aiconversation.data.exception

class DeserializationException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)