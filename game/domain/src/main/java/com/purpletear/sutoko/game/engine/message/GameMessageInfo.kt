package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageInfo(
    id: String,
    val text: String,
) : GameMessage(
    id = id,
    type = GameMessageType.Info,
)