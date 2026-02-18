package com.purpletear.ai_conversation.domain.enums

sealed class MessageType {
    object User : MessageType()
    object Bot : MessageType()
}