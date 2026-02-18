package com.purpletear.ai_conversation.domain.enums

enum class ConversationMode(val code: String) {
    Sms("sms"),
    Irl("irl");

    companion object {
        fun fromString(value: String): ConversationMode? {
            return ConversationMode.entries.find { it.code.lowercase() == value.lowercase() }
        }
    }
}

