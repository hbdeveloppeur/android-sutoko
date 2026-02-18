package com.purpletear.ai_conversation.domain.enums

sealed class MessageState(val code: String) {
    data object Idle : MessageState("idle")
    data object PreSending : MessageState("presending")
    data object Sending : MessageState("sending")
    data object Sent : MessageState("sent")
    data object Seen : MessageState("seen")
    data object Failed : MessageState("failed")
    companion object {
        fun fromString(value: String?): MessageState? {
            return when (value) {
                "idle" -> MessageState.Idle
                "presending" -> MessageState.PreSending
                "sending" -> MessageState.Sending
                "sent" -> MessageState.Sent
                "seen" -> MessageState.Seen
                "failed" -> MessageState.Failed
                else -> null
            }
        }
    }
}

