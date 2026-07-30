package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import kotlin.math.sin

@Preview(name = "GameMessageVocalDest")
@Composable
private fun Preview() {
    val character = PreviewCharacter.copy(
        color = CharacterColor(
            startingColor = "#8E2DE2",
            endingColor = "#4A00E0",
        )
    )
    Box(Modifier.padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MessageVocalDest(character = character, isPlaying = false, percent = 0f)
            MessageVocalDest(character = character, isPlaying = true, percent = 0.45f)
            MessageVocalDest(
                character = character,
                isPlaying = false,
                percent = 1f,
                isRightSide = true,
            )
        }
    }
}

@Composable
internal fun MessageVocalDest(
    character: Character,
    isPlaying: Boolean,
    percent: Float,
    /** Display side of the bubble; vocal bubbles were historically left-aligned. */
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
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start,
        ) {
            Header(character, isRightSide)
            val bubbleShape = RoundedCornerShape(22.dp)
            Box(
                Modifier
                    .clip(bubbleShape)
                    .clickable(onClick = onClick)
            ) {
                MessageBubble(shape = bubbleShape) {
                    PlayButton(isPlaying, onClick)
                    Waveform(
                        percent = percent,
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(character: Character, isRightSide: Boolean) {
    val accentColor = character.color.toWhitenedComposeColor(fraction = 0.7f)
    val nameColor = character.color.toWhitenedComposeColor(fraction = 0.6f)
    Row(
        modifier = Modifier.padding(
            start = if (isRightSide) 0.dp else 8.dp,
            end = if (isRightSide) 8.dp else 0.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val avatar: @Composable () -> Unit = {
            Avatar(
                modifier = Modifier.background(accentColor, CircleShape),
                size = 22.dp,
                borderWidth = 1.4.dp,
                borderColor = accentColor,
                imageModel = character.avatar
            )
        }
        val name: @Composable () -> Unit = {
            Text(
                text = character.name,
                color = nameColor,
                fontFamily = WorkSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        if (isRightSide) {
            name()
            avatar()
        } else {
            avatar()
            name()
        }
    }
}

/** Normalized bar heights (0..1) giving the waveform a natural voice-like envelope. */
private val WaveformPattern = listOf(
    0.30f, 0.55f, 0.80f, 0.60f, 0.95f, 0.70f, 0.45f,
    0.75f, 1.00f, 0.65f, 0.40f, 0.70f, 0.90f, 0.55f, 0.35f
)

private val WaveformMaxHeight = 24.dp
private val WaveformMinHeight = 6.dp

@Composable
private fun Waveform(percent: Float, isPlaying: Boolean, modifier: Modifier = Modifier) {
    val clampedPercent = percent.coerceIn(0f, 1f)
    val scaledProgress = clampedPercent * WaveformPattern.size

    // Gentle "breathing" while playing; frozen (and free) otherwise.
    val phase = if (isPlaying) {
        val transition = rememberInfiniteTransition(label = "waveformPhase")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "waveformPhase",
        ).value
    } else 0f

    Row(
        modifier = modifier
            .height(32.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WaveformPattern.forEachIndexed { index, fraction ->
            val itemProgress = (scaledProgress - index).coerceIn(0f, 1f)
            val pulse = if (isPlaying) {
                1f + 0.12f * sin(phase + index * 0.9f)
            } else 1f
            WaveformBar(
                height = WaveformMinHeight + (WaveformMaxHeight - WaveformMinHeight) * fraction,
                progress = itemProgress,
                scaleY = pulse,
            )
        }
    }
}

@Composable
private fun WaveformBar(height: Dp, progress: Float, scaleY: Float) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        Modifier
            .graphicsLayer { this.scaleY = scaleY }
            .height(height)
            .width(3.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.35f))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color.White)
        )
    }
}

@Composable
private fun PlayButton(isPlaying: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (isPlaying) R.drawable.game_presentation_ic_pause_button else R.drawable.game_presentation_ic_play_button),
            contentDescription = stringResource(R.string.game_presentation_message_vocal_play_description),
            modifier = Modifier.size(22.dp)
        )
    }
}
