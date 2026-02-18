package com.purpletear.aiconversation.presentation.component.blurred_message

sealed class MessageCornerType {
    data object First : MessageCornerType()
    data object Last : MessageCornerType()
    data object Middle : MessageCornerType()
}