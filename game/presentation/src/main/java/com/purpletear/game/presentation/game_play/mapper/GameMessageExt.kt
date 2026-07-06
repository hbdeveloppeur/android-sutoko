package com.purpletear.game.presentation.game_play.mapper

import com.purpletear.game.presentation.game_play.components.message.MessagePositionInGroup
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

internal fun GameMessage.positionInGroup(
    previousMessage: GameMessage?,
    nextMessage: GameMessage?,
): MessagePositionInGroup {
    val sameAsAbove = hasSameCharacter(previousMessage)
    val sameAsBelow = hasSameCharacter(nextMessage)
    return when {
        !sameAsAbove && !sameAsBelow -> MessagePositionInGroup.SINGLE
        !sameAsAbove && sameAsBelow -> MessagePositionInGroup.TOP
        sameAsAbove && sameAsBelow -> MessagePositionInGroup.MIDDLE
        else -> MessagePositionInGroup.BOTTOM
    }
}
