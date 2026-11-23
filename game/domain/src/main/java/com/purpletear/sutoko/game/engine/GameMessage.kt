package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

/**
 * Represents a message in the game conversation (domain model).
 * The status drives UI behavior like typing indicators.
 */
@Keep
open class GameMessage(
    var id: String,
    val type: GameMessageType,
)
