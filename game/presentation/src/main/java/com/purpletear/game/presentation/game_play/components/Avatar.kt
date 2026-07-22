package com.purpletear.game.presentation.game_play.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.debug.R as DebugR

@Preview(name = "GameAvatar")
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .height(52.dp)
            .aspectRatio(564f / 166f),
        drawable = DebugR.drawable.game_debug_preview_messagedest,
    ) {
        Box(Modifier.padding(start = 14.dp)) {
            Avatar(
                size = 24.dp,
                imageModel = R.drawable.tmp_avatar
            )
        }
    }
}

@Composable
internal fun Avatar(
    modifier: Modifier = Modifier,
    size: Dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White,
    imageModel: Any?,
    @DrawableRes fallbackDrawable: Int = R.drawable.tmp_avatar
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .circularBorder(borderColor, borderWidth),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
            model = imageModel ?: fallbackDrawable,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

private fun Modifier.circularBorder(color: Color, width: Dp): Modifier = drawWithContent {
    drawContent()
    if (width <= 0.dp) return@drawWithContent
    val strokeWidth = width.toPx()
    drawCircle(
        color = color,
        radius = (size.width - strokeWidth) / 2,
        style = Stroke(width = strokeWidth)
    )
}
