package com.purpletear.aiconversation.presentation.component.blurred_message

sealed class MessagePositionInGroup {
    data object PositionSingle : MessagePositionInGroup()
    data object PositionFirst : MessagePositionInGroup()
    data object PositionMiddle : MessagePositionInGroup()
    data object PositionLast : MessagePositionInGroup()
}