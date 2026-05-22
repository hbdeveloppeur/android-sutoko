package com.purpletear.game.presentation.game_play.mapper

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.message.GameMessageVocal

internal fun GameMessage.characterId(): Int? = when (this) {
    is GameMessageText -> characterId
    is GameMessageImage -> characterId
    is GameMessageTyping -> characterId
    is GameMessageVocal -> characterId
    else -> null
}

internal fun GameMessage.hasSameCharacter(other: GameMessage?): Boolean {
    if (other == null) return false
    val id = this.characterId()
    return id != null && id == other.characterId()
}
