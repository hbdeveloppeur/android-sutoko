package com.purpletear.sutoko.notification.sealed

sealed class Screen(val id: String?) {
    data object Unspecified : Screen(null)
    data object Home : Screen(null)
    data object Main : Screen(null)
    data class Conversation(val characterId: Int) : Screen(characterId.toString())
}