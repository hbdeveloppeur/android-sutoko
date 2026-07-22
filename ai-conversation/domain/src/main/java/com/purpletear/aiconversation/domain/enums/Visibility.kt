package com.purpletear.aiconversation.domain.enums

import androidx.annotation.Keep

@Keep
enum class Visibility(val value: String) {
    Private("private"),
    Public("public");

    companion object {
        fun fromString(value: String): Visibility? {
            return entries.find { it.value.lowercase() == value.lowercase() }
        }
    }
}