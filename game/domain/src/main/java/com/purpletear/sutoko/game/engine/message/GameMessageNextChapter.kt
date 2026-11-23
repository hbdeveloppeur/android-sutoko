package com.purpletear.sutoko.game.engine.message

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Keep
class GameMessageNextChapter(
    id: String = "next_chapter",
) : GameMessage(
    id = id,
    type = GameMessageType.ChapterEnd,
)