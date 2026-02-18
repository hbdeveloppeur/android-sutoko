package com.purpletear.aiconversation.data.exception

class NoBannerFoundException(
    message: String? = null,
    cause: Throwable? = null
) : ApiException(message, cause)