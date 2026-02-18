package com.purpletear.aiconversation.data.exception

class UnableToReachServiceException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)