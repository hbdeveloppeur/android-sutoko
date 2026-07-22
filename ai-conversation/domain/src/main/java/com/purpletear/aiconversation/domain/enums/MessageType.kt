package com.purpletear.aiconversation.domain.enums

import androidx.annotation.Keep

@Keep
sealed class MessageType {
    object User : MessageType()
    object Bot : MessageType()
}