package com.purpletear.game.presentation.game_play.mapper

import androidx.compose.runtime.Composable
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

@Composable
internal fun Message(message: GameMessage) {
    when (message.type) {
        GameMessageType.Text -> {
            MessageText(text = message.text)
        }
    }
}