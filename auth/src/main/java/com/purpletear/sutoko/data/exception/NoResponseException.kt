package com.purpletear.sutoko.data.exception

class NoResponseException(
    message: String = "No response body exception",
    cause: Throwable? = null,
) : Exception(message, cause)
