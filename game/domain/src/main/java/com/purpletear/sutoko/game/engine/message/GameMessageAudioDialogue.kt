package com.purpletear.sutoko.game.engine.message

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Keep
class GameMessageAudioDialogue(
    id: String,
    val audioUrl: String,
    val characterId: Int,
    val text: String,
) : GameMessage(
    id = id,
    type = GameMessageType.AudioDialogue,
)
