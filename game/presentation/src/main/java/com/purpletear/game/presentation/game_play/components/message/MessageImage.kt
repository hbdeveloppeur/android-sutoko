package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.debug.PreviewCharacter
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
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16.dp)
    val imageRequest = ImageRequest.Builder(context)
        .data(path)
        .crossfade(300)
        .build()


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
        ) {
            AsyncImage(
                modifier = Modifier
                    .matchParentSize(),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        Avatar(
            modifier = Modifier
                .background(character.avatarColor())
                .align(alignment),
            size = 26.dp,
            borderWidth = 1.4.dp,
            imageModel = character.avatar
        )
    }
}

private fun Character.avatarColor(): Color {
    return color.startingColor.toComposeColor() ?: Color.Blue
}

private fun String.toComposeColor(): Color? = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    null
}

