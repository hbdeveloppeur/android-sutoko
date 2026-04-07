package com.purpletear.game.presentation.game_play.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.purpletear.game.presentation.game_play.components.message.MessageNextChapter
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.game.presentation.game_play.components.message.MessageTyping
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping

@Composable
internal fun Message(
    message: GameMessage,
    modifier: Modifier = Modifier
) {
    when (message.type) {
        GameMessageType.Text -> {
            message as GameMessageText
            MessageText(text = message.text, modifier = modifier)
        }

        GameMessageType.Typing -> {
            message as GameMessageTyping
            // TODO : change isMainCharacter given the characterId
            MessageTyping(modifier = modifier)
        }

        GameMessageType.ChapterEnd -> {
            MessageNextChapter(modifier = modifier)
        }
    }
}