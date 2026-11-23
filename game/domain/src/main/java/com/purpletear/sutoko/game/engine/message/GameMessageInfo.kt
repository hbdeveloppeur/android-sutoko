package com.purpletear.sutoko.game.engine.message

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Keep
class GameMessageInfo(
    id: String,
    val text: String,
) : GameMessage(
    id = id,
    type = GameMessageType.Info,
)