package com.purpletear.sutoko.game.engine

/**
 * Represents a message item in the game conversation.
 */
data class MessageItem(
    val id: String,
    val text: String,
    val characterId: Int,
    val isMainCharacter: Boolean
)
