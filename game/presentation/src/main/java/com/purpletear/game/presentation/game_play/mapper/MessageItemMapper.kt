package com.purpletear.game.presentation.game_play.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.purpletear.game.presentation.game_play.components.message.MessageImage
import com.purpletear.game.presentation.game_play.components.message.MessageNarration
import com.purpletear.game.presentation.game_play.components.message.MessageNextChapter
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.game.presentation.game_play.components.message.MessageTyping
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.model.character.Character

@Composable
internal fun Message(
    modifier: Modifier = Modifier,
    message: GameMessage,
    character: Character? = null,
) {
    when (message.type) {
        GameMessageType.Text -> {
            assert(character != null)
            message as GameMessageText
            MessageText(text = message.text, character = character!!, modifier = modifier)
        }

        GameMessageType.Typing -> {
            message as GameMessageTyping
            MessageTyping(character = character, modifier = modifier)
        }

        GameMessageType.ChapterEnd -> {
            MessageNextChapter(modifier = modifier)
        }

        GameMessageType.Info -> {
            message as GameMessageInfo
            MessageNarration(text = message.text)
        }

        GameMessageType.Image -> {
            assert(character != null)
            message as GameMessageImage
            MessageImage(path = message.imageUrl, character = character!!)
        }
    }
}