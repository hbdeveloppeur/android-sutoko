package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageTyping(
    id: String,
    val characterId: Int,
    val backgroundColor: String? = null,
    val foregroundColor: String? = null,
) : GameMessage(
    id = id,
    type = GameMessageType.Typing,
)