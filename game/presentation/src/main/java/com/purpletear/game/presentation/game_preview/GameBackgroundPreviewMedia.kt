package com.purpletear.game.presentation.game_preview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.R

/**
 * A composable that displays a background media stack: video > image > scrim.
 *
 * The video sits under the image. Once the video renders its first frame,
 * the image fades out to reveal it (a crossfade, no black gap).
 * If the video never starts, the image simply remains on screen.
 *
 * @param imageUrl Optional URL of the background image.
 * @param videoUrl Optional URL of the background video.
 * @param modifier The modifier to be applied to the root container.
 * @param overlayAlpha Alpha of the black scrim drawn on top of everything. Default is 0.1f.
 * @param fallbackPainter Optional painter shown when neither an image nor a video is available.
 */
@Composable
internal fun GameBackgroundPreviewMedia(
    imageUrl: String?,
    videoUrl: String?,
    modifier: Modifier = Modifier,
    overlayAlpha: Float = 0.1f,
    fallbackPainter: Painter? = null,
) {
    val effectiveImageUrl = imageUrl?.takeIf { it.isNotBlank() }
    val effectiveVideoUrl = videoUrl?.takeIf { it.isNotBlank() }

    // Reset when the video changes; stays false if the video fails to start.
    var videoStarted by remember(effectiveVideoUrl) { mutableStateOf(false) }
    val imageAlpha by animateFloatAsState(
        targetValue = if (videoStarted) 0f else 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "imageAlpha"
    )

    val context = LocalContext.current
    val errorPainter = remember { ColorPainter(Color.DarkGray) }

    Box(modifier = modifier.fillMaxSize()) {
        // Video stays under the image; the image fade reveals it (no black gap).
        effectiveVideoUrl?.let { url ->
            BackgroundMedia(
                videoUrl = url,
                onFirstFrame = { videoStarted = true },
                modifier = Modifier.fillMaxSize()
            )
        }

        effectiveImageUrl?.let { url ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.game_presentation_game_preview_background_description),
                contentScale = ContentScale.Crop,
                error = errorPainter,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha)
            )
        }

        if (effectiveImageUrl == null && effectiveVideoUrl == null) {
            fallbackPainter?.let { painter ->
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.game_presentation_game_preview_background_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
        )
    }
}
