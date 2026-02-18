package com.purpletear.aiconversation.data.exception

class ServerErrorException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)