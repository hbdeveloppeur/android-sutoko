package com.purpletear.game.presentation.game_play.components.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
 * @param onLoaded Callback invoked when the image has loaded successfully
 * @param onError Callback invoked when image loading fails, with the error details
 */
@Composable
fun ImageBackground(
    imagePath: String,
    modifier: Modifier = Modifier,
    onLoaded: () -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val loadedCallback by rememberUpdatedState(onLoaded)
    val errorCallback by rememberUpdatedState(onError)
    val context = LocalContext.current
    val file = File(imagePath)

    val imageRequest = remember(imagePath) {
        ImageRequest.Builder(context)
            .data(if (file.exists()) file else imagePath)
            .crossfade(false)
            .listener(
                onSuccess = { _, _ -> loadedCallback() },
                onError = { _, result ->
                    errorCallback(result.throwable)
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
