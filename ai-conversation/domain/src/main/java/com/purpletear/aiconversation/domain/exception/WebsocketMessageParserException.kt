package com.purpletear.aiconversation.domain.exception

class WebsocketMessageParserException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)