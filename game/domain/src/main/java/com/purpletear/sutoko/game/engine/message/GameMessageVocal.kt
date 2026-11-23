package com.purpletear.sutoko.game.engine.message

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Keep
class GameMessageVocal(
    id: String,
    val audioUrl: String,
    val characterId: Int,
) : GameMessage(
    id = id,
    type = GameMessageType.Vocal,
)