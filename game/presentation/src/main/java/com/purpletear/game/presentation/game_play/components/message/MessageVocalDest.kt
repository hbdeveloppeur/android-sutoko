package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.sutoko.game.model.character.Character

@Composable
internal fun MessageVocalDest(
    character: Character, isPlaying: Boolean, percent: Float, onClick: () -> Unit = {}
) {
    MessageBubble(Modifier.padding(end = 4.dp)) {
        Avatar(
            modifier = Modifier.background(Color.Blue),
            size = 22.dp,
            borderWidth = 1.4.dp,
            imageModel = character.avatar
        )
        Progress(percent)
        PlayButton(isPlaying, onClick)
    }
}

@Composable
private fun Progress(percent: Float) {
    val heights = listOf(14.dp, 22.dp, 16.dp, 24.dp, 12.dp, 16.dp, 8.dp)
    val clampedPercent = percent.coerceIn(0f, 1f)
    val scaledProgress = clampedPercent * heights.size

    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        heights.forEachIndexed { index, height ->
            val itemProgress = (scaledProgress - index).coerceIn(0f, 1f)
            ProgressBarItem(
                height = height,
                progress = itemProgress
            )
        }
    }
}

@Composable
private fun ProgressBarItem(height: Dp, progress: Float) {
    val width = 4.dp
    val shape = RoundedCornerShape(2.dp)
    Box(
        Modifier
            .height(height)
            .width(width)
            .clip(shape)
            .background(Color.White.copy(0.4f))
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
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause_button else R.drawable.ic_play_button),
            contentDescription = stringResource(R.string.message_vocal_play_description),
            modifier = Modifier
                .size(22.dp)
                .clip(shape)
        )
    }
}
