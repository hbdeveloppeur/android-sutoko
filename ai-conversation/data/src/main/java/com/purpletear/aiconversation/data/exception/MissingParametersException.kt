package com.purpletear.aiconversation.data.exception

class MissingParametersException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)