package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.WorkSansFontFamily
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.extensions.toWhitenedComposeColor
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.game.presentation.game_play.mapper.ITEMS_HORIZONTAL_PADDING
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor

@Preview(name = "GameMessageAudioDialogue")
@Composable
private fun Preview() {
    val character = PreviewCharacter.copy(
        color = CharacterColor(
            startingColor = "#8E2DE2",
            endingColor = "#4A00E0",
        )
    )
    Box(Modifier.padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MessageAudioDialogue(
                character = character,
                text = "Je t'attendais depuis tout ce temps, tu sais.",
                isPlaying = false,
                percent = 0f,
            )
            MessageAudioDialogue(
                character = character,
                text = "Je t'attendais depuis tout ce temps, tu sais.",
                isPlaying = true,
                percent = 0.45f,
            )
            MessageAudioDialogue(
                character = character,
                text = "Je t'attendais depuis tout ce temps, tu sais.",
                isPlaying = false,
                percent = 1f,
                isRightSide = true,
            )
        }
    }
}

/**
 * Dialogue spoken by [character]: avatar with a subtle play/pause badge, and [text] whose
 * letters turn from low-alpha white to full white as the audio progresses ([percent]).
 */
@Composable
internal fun MessageAudioDialogue(
    character: Character,
    text: String,
    isPlaying: Boolean,
    percent: Float,
    isRightSide: Boolean = false,
    onClick: () -> Unit = {}
) {
    val alignment = if (isRightSide) Alignment.CenterEnd else Alignment.CenterStart
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = ITEMS_HORIZONTAL_PADDING),
        contentAlignment = alignment,
    ) {
        Row(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val avatar: @Composable () -> Unit = {
                AvatarWithPlayBadge(character, isPlaying)
            }
            val dialogue: @Composable () -> Unit = {
                Text(
                    text = highlightedDialogue(text, percent),
                    fontFamily = WorkSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                )
            }
            if (isRightSide) {
                dialogue()
                avatar()
            } else {
                avatar()
                dialogue()
            }
        }
    }
}

@Composable
private fun AvatarWithPlayBadge(character: Character, isPlaying: Boolean) {
    val accentColor = character.color.toWhitenedComposeColor(fraction = 0.7f)
    Box {
        Avatar(
            size = 40.dp,
            borderWidth = 1.4.dp,
            borderColor = accentColor,
            imageModel = character.avatar,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .background(accentColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.game_presentation_ic_pause_button
                    else R.drawable.game_presentation_ic_play_button
                ),
                contentDescription = stringResource(R.string.game_presentation_message_vocal_play_description),
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

private fun highlightedDialogue(text: String, percent: Float): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val highlightCount = (percent.coerceIn(0f, 1f) * text.length).toInt()
    return buildAnnotatedString {
        withStyle(SpanStyle(color = Color.White)) {
            append(text.substring(0, highlightCount))
        }
        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.35f))) {
            append(text.substring(highlightCount))
        }
    }
}
