package com.purpletear.aiconversation.data.exception

open class ApiException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)