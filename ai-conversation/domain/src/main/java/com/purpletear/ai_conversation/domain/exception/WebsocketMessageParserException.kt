package com.purpletear.ai_conversation.domain.exception

class WebsocketMessageParserException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)