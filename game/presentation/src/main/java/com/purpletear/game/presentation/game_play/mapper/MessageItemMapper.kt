package com.purpletear.game.presentation.game_play.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.message.FadeInMessageContainer
import com.purpletear.game.presentation.game_play.components.message.MessageImage
import com.purpletear.game.presentation.game_play.components.message.MessageNarration
import com.purpletear.game.presentation.game_play.components.message.MessageNextChapter
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.game.presentation.game_play.components.message.MessageTyping
import com.purpletear.game.presentation.game_play.components.message.MessageVocalDest
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.message.GameMessageVocal
import com.purpletear.sutoko.game.model.character.Character

@Composable
internal fun Message(
    modifier: Modifier = Modifier,
    previousMessage: GameMessage?,
    message: GameMessage,
    character: Character? = null,
    currentVocalUrl: String? = null,
    isVocalPlaying: Boolean = false,
    vocalProgress: Float = 0f,
    onImageClick: (imageUrl: String, bounds: Rect) -> Unit = { _, _ -> },
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
    onNextChapterClick: () -> Unit = {},
    showNextChapterButton: Boolean = true,
    nextChapterTitleRes: Int? = null,
    onVocalClick: (String) -> Unit = {},
) {
    FadeInMessageContainer(modifier = modifier) {
        when (message.type) {
            GameMessageType.Text -> {
                assert(character != null)
                message as GameMessageText
                MessageText(
                    text = message.text,
                    character = character!!,
                    showHeader = !message.hasSameCharacter(previousMessage),
                    onAvatarClick = onAvatarClick,
                )
            }

            GameMessageType.Typing -> {
                message as GameMessageTyping
                MessageTyping(character = character)
            }

            GameMessageType.ChapterEnd -> {
                val title = nextChapterTitleRes?.let { stringResource(it) }
                    ?: stringResource(R.string.message_next_chapter_title)
                MessageNextChapter(
                    title = title,
                    showButton = showNextChapterButton,
                    onClick = onNextChapterClick
                )
            }

            GameMessageType.Info -> {
                message as GameMessageInfo
                val text = if (message.id == "end_story") {
                    stringResource(R.string.message_story_finished)
                } else {
                    message.text
                }
                MessageNarration(text = text)
            }

            GameMessageType.Image -> {
                assert(character != null)
                message as GameMessageImage
                MessageImage(
                    path = message.imageUrl,
                    character = character!!,
                    onClick = { bounds -> onImageClick(message.imageUrl, bounds) }
                )
            }

            GameMessageType.Vocal -> {
                assert(character != null)
                message as GameMessageVocal
                val isThisPlaying = message.audioUrl == currentVocalUrl && isVocalPlaying
                val percent = if (message.audioUrl == currentVocalUrl) vocalProgress else 0f
                MessageVocalDest(
                    isPlaying = isThisPlaying,
                    character = character!!,
                    percent = percent,
                    onClick = { onVocalClick(message.audioUrl) }
                )
            }
        }
    }
}
