package com.purpletear.aiconversation.data.exception

class MalformedResponseException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)