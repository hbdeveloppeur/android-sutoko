package com.purpletear.aiconversation.presentation.component.character.avatar.character_avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl

@Composable
internal fun CharacterAvatar(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap? = null,
    orUrl: String,
    isSelected: Boolean = false,
    size: Dp,
    strokeColor : Color = Color.White,
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(modifier)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF040617))
            .border(
                (if (isSelected) 2 else 1).dp, strokeColor.copy(
                    if (isSelected) 1f else 0.5f
                ), MaterialTheme.shapes.medium
            )
            .alpha(if (isSelected) 1f else 0.7f),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        AsyncImage(
            model =
            ImageRequest.Builder(LocalContext.current)
                .data(getRemoteAssetsUrl(orUrl))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Imported avatar",
                contentScale = ContentScale.Crop
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF292929).copy(
                        if (isSelected) 0f else 0.5f
                    )
                )
        )
    }
}