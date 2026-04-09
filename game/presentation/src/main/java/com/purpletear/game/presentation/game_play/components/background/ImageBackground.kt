package com.purpletear.game.presentation.game_play.components.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Displays an image background using Coil.
 * Image scales to fill the container (crop behavior) using ContentScale.Crop.
 * Notifies parent when image loads successfully and propagates errors.
 *
 * @param imagePath The absolute path to the image file
 * @param modifier The modifier to be applied to the component
 * @param onStarted Callback invoked when image starts loading successfully
 * @param onError Callback invoked when image loading fails, with the error details
 */
@Composable
fun ImageBackground(
    imagePath: String,
    modifier: Modifier = Modifier,
    onStarted: () -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current
    val file = File(imagePath)

    val imageRequest = remember(imagePath) {
        ImageRequest.Builder(context)
            .data(if (file.exists()) file else imagePath)
            .crossfade(false)
            .listener(
                onStart = { onStarted() },
                onError = { _, result ->
                    onError(result.throwable)
                }
            )
            .build()
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
