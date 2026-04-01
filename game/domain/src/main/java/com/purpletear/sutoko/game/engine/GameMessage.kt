package com.purpletear.sutoko.game.engine

/**
 * Represents a message in the game conversation (domain model).
 * The status drives UI behavior like typing indicators.
 */
open class GameMessage(
    var id: String,
    val text: String,
    val characterId: Int,
    val type: GameMessageType,
)
