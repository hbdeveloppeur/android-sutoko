package com.purpletear.ai_conversation.domain.enums

enum class Visibility(val value: String) {
    Private("private"),
    Public("public");

    companion object {
        fun fromString(value: String): Visibility? {
            return entries.find { it.value.lowercase() == value.lowercase() }
        }
    }
}