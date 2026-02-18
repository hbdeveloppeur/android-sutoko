package com.purpletear.aiconversation.domain.enums

enum class CharacterStatus(val code: String) {
    Online("Online"),
    Offline("Offline"),
    BlockedYou("BlockedYou");

    companion object {
        fun fromString(value: String): CharacterStatus? {
            return CharacterStatus.entries.find { it.code.lowercase() == value.lowercase() }
        }
    }
}

