package com.purpletear.sutoko.notification.sealed

import androidx.annotation.Keep

sealed class Screen(val id: String?) {
    data object Unspecified : Screen(null)
    data object Home : Screen(null)
    data object Main : Screen(null)
    @Keep
    data class Conversation(val characterId: Int) : Screen(characterId.toString())
}