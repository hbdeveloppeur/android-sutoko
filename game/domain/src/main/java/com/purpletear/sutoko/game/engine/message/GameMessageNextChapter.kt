package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

class GameMessageNextChapter(
    id: String = "next_chapter",
) : GameMessage(
    id = id,
    type = GameMessageType.ChapterEnd,
)