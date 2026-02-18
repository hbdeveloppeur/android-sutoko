package com.purpletear.ai_conversation.domain.enums

sealed class MessageRole(val code: String) {
    data object User : MessageRole("user")
    data object Assistant : MessageRole("assistant")
    data object Narrator : MessageRole("narrator")
    companion object {
        fun fromString(value: String?): MessageRole? {
            return when (value) {
                User.code -> User
                Assistant.code -> Assistant
                Narrator.code -> Narrator
                else -> null
            }
        }
    }
}
