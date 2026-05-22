package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageVocal(
    id: String,
    val audioUrl: String,
    val characterId: Int,
) : GameMessage(
    id = id,
    type = GameMessageType.Vocal,
)