package com.purpletear.aiconversation.data.exception

class NoResponseException(
    message: String = "No response body exception",
    cause: Throwable? = null
) : ApiException(message, cause)