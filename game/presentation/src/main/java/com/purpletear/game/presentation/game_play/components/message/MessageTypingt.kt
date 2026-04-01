package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R

@Preview(name = "GameMessageTyping")
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .height(36.dp)
            .aspectRatio(82f / 42f),
        drawable = R.drawable.preview_typing,
    ) {
        Column(Modifier.padding(4.dp)) {
            MessageTyping()
            MessageTyping(isMainCharacter = true)
        }
    }
}

private const val ANIMATION_DURATION = 1200
private const val DOT_COUNT = 3
private const val DOT_DELAY = ANIMATION_DURATION / DOT_COUNT

@Composable
internal fun MessageTyping(isMainCharacter: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val alignment = if (isMainCharacter) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        Modifier
            .fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        MessageBubble {
            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(DOT_COUNT) { index ->
                    val alpha = infiniteTransition.dotAlphaAnimation(
                        delayMillis = index * DOT_DELAY
                    )
                    Dot(alpha = alpha.value)
                }
            }
        }
    }
}

@Composable
private fun InfiniteTransition.dotAlphaAnimation(
    delayMillis: Int
): State<Float> = animateFloat(
    initialValue = 0.4f,
    targetValue = 0.4f,
    animationSpec = infiniteRepeatable(
        animation = keyframes {
            durationMillis = ANIMATION_DURATION
            0.4f at 0
            1f at (ANIMATION_DURATION / 2) with androidx.compose.animation.core.LinearEasing
            0.4f at ANIMATION_DURATION
        },
        repeatMode = RepeatMode.Restart,
        initialStartOffset = androidx.compose.animation.core.StartOffset(delayMillis)
    ),
    label = "dot_alpha_$delayMillis"
)

@Composable
internal fun Dot(alpha: Float) {
    Box(
        Modifier
            .size(8.dp)
            .clip(CircleShape)
            .alpha(alpha)
            .background(Color.White)
    )
}