package com.purpletear.sutoko.game.engine.message

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Keep
class GameMessageImage(
    id: String,
    val imageUrl: String,
    val characterId: Int,
) : GameMessage(
    id = id,
    type = GameMessageType.Image,
)
