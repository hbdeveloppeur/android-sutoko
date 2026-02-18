package com.purpletear.aiconversation.domain.enums

sealed class MessageType {
    object User : MessageType()
    object Bot : MessageType()
}