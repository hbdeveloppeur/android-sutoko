package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageText(
    id: String,
    text: String,
    characterId: Int,
) : GameMessage(
    id = id,
    text = text,
    characterId = characterId,
    type = GameMessageType.Text,
)