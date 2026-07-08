package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.common.extensions.toWhitenedComposeColor
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.sutoko.game.model.character.Character

@Preview(name = "MessageImage")
@Composable
private fun Preview() {
    Box(
        Modifier
            .padding(4.dp)
    ) {
        MessageImage(
            path = "https://data.sutoko.app/resources/sutoko-ai/image/AiChatHomePageHeader.jpg",
            character = PreviewCharacter,
        )
    }
}


@Composable
internal fun MessageImage(
    path: String,
    character: Character,
    onClick: (bounds: Rect) -> Unit = {},
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16.dp)
    val imageRequest = ImageRequest.Builder(context)
        .data(path)
        .crossfade(300)
        .build()

    var bounds by remember { mutableStateOf(Rect.Zero) }

    val alignment =
        if (character.isMainCharacter) Alignment.BottomEnd else Alignment.BottomStart
    Box(Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .height(196.dp)
                .width(146.dp)
                .padding(horizontal = 4.dp)
                .padding(bottom = 8.dp)
                .border(width = 1.dp, color = Color.White.copy(0.15f), shape = shape)
                .clip(shape)
                .onGloballyPositioned { coordinates ->
                    bounds = coordinates.boundsInWindow()
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onClick(bounds) }
                )
        ) {
            AsyncImage(
                modifier = Modifier
                    .matchParentSize(),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        val avatarColor = character.color.toWhitenedComposeColor(fraction = 0.7f)
        Avatar(
            modifier = Modifier
                .background(avatarColor, CircleShape)
                .align(alignment),
            size = 26.dp,
            borderWidth = 1.4.dp,
            borderColor = avatarColor,
            imageModel = character.avatar
        )
    }
}



