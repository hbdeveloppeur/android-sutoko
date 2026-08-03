package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageAudioDialogue(
    id: String,
    val audioUrl: String,
    val characterId: Int,
    val text: String,
) : GameMessage(
    id = id,
    type = GameMessageType.AudioDialogue,
)
