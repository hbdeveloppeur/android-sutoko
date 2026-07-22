package com.purpletear.game.presentation.game_play.mapper

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.message.FadeInMessageContainer
import com.purpletear.game.presentation.game_play.components.message.MessageChapterTrialFinished
import com.purpletear.game.presentation.game_play.components.message.MessageImage
import com.purpletear.game.presentation.game_play.components.message.MessageManga
import com.purpletear.game.presentation.game_play.components.message.MessageNarration
import com.purpletear.game.presentation.game_play.components.message.MessageNextChapter
import com.purpletear.game.presentation.game_play.components.message.MessagePositionInGroup
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.game.presentation.game_play.components.message.MessageTyping
import com.purpletear.game.presentation.game_play.components.message.MessageVocalDest
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.message.GameMessageVocal
import com.purpletear.sutoko.game.model.character.Character

internal val ITEMS_HORIZONTAL_PADDING = 16.dp

private const val MESSAGE_CROSSFADE_DURATION_MS = 180

@Composable
internal fun Message(
    modifier: Modifier = Modifier,
    previousMessage: GameMessage?,
    nextMessage: GameMessage?,
    message: GameMessage,
    character: Character? = null,
    isNewlyAdded: Boolean = false,
    currentVocalUrl: String? = null,
    isVocalPlaying: Boolean = false,
    vocalProgress: Float = 0f,
    onImageClick: (imageUrl: String, bounds: Rect) -> Unit = { _, _ -> },
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
    onMangaClick: (imageUrl: String, overlays: List<GameMessageMangaPage.TextOverlay>) -> Unit = { _, _ -> },
    onNextChapterClick: () -> Unit = {},
    showNextChapterButton: Boolean = true,
    nextChapterTitleRes: Int? = null,
    isTrial: Boolean = false,
    gameLogoUrl: String? = null,
    onBackClick: () -> Unit = {},
    onVocalClick: (String) -> Unit = {},
) {
    FadeInMessageContainer(animate = isNewlyAdded, modifier = modifier) {
        Crossfade(
            targetState = message,
            animationSpec = tween(MESSAGE_CROSSFADE_DURATION_MS),
            label = "message",
        ) { msg ->
            when (msg.type) {
                GameMessageType.Text -> {
                    assert(character != null)
                    msg as GameMessageText
                    val positionInGroup = msg.positionInGroup(previousMessage, nextMessage)
                    MessageText(
                        text = msg.text,
                        character = character!!,
                        showHeader = positionInGroup != MessagePositionInGroup.MIDDLE && positionInGroup != MessagePositionInGroup.BOTTOM,
                        positionInGroup = positionInGroup,
                        bubbleColorHex = msg.backgroundColor,
                        textColorHex = msg.foregroundColor,
                        onAvatarClick = onAvatarClick,
                    )
                }

                GameMessageType.Typing -> {
                    msg as GameMessageTyping
                    MessageTyping(
                        character = character,
                        bubbleColorHex = msg.backgroundColor,
                        textColorHex = msg.foregroundColor,
                    )
                }

                GameMessageType.ChapterEnd -> {
                    if (isTrial) {
                        MessageChapterTrialFinished(
                            gameLogoUrl = gameLogoUrl.orEmpty(),
                            onClick = onBackClick,
                        )
                    } else {
                        val title = nextChapterTitleRes?.let { stringResource(it) }
                            ?: stringResource(R.string.game_presentation_message_next_chapter_title)
                        MessageNextChapter(
                            title = title,
                            showButton = showNextChapterButton,
                            onClick = onNextChapterClick
                        )
                    }
                }

                GameMessageType.Info -> {
                    msg as GameMessageInfo
                    val text = if (msg.id == "end_story") {
                        stringResource(R.string.game_presentation_message_story_finished)
                    } else {
                        msg.text
                    }
                    MessageNarration(text = text)
                }

                GameMessageType.Image -> {
                    assert(character != null)
                    msg as GameMessageImage
                    MessageImage(
                        path = msg.imageUrl,
                        character = character!!,
                        onClick = { bounds -> onImageClick(msg.imageUrl, bounds) }
                    )
                }

                GameMessageType.MangaPage -> {
                    msg as GameMessageMangaPage
                    MessageManga(onClick = { onMangaClick(msg.imageUrl, msg.overlays) })
                }

                GameMessageType.Vocal -> {
                    assert(character != null)
                    msg as GameMessageVocal
                    val isThisPlaying = msg.audioUrl == currentVocalUrl && isVocalPlaying
                    val percent = if (msg.audioUrl == currentVocalUrl) vocalProgress else 0f
                    MessageVocalDest(
                        isPlaying = isThisPlaying,
                        character = character!!,
                        percent = percent,
                        onClick = { onVocalClick(msg.audioUrl) }
                    )
                }
            }
        }
    }
}
