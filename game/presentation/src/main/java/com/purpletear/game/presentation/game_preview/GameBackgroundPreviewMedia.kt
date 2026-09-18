package com.purpletear.game.presentation.game_preview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

    val context = LocalContext.current
    val errorPainter = remember { ColorPainter(Color.DarkGray) }
    val coverPainter = fallbackPainter ?: errorPainter
    val imageRequest = remember(context, effectiveImageUrl) {
        ImageRequest.Builder(context)
            .data(effectiveImageUrl)
            .crossfade(true)
            .build()
    }

    key(effectiveVideoUrl) {
        var videoStarted by remember { mutableStateOf(false) }
        val coverAlpha = animateFloatAsState(
            targetValue = if (videoStarted) 0f else 1f,
            animationSpec = if (videoStarted) tween(durationMillis = 500) else snap(),
            label = "previewMediaCoverAlpha",
        )

        Box(modifier = modifier.fillMaxSize()) {
            effectiveVideoUrl?.let { url ->
                val playback = rememberPreviewVideoPlayback()
                if (playback.attachPlayer) {
                    BackgroundMedia(
                        videoUrl = url,
                        playWhenReady = playback.playWhenReady,
                        onFirstFrame = { videoStarted = true },
                        onError = { videoStarted = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    // Restore an opaque cover immediately on failure; only revealing video fades.
                    alpha = if (videoStarted) coverAlpha.value else 1f
                },
            ) {
                if (effectiveImageUrl != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = stringResource(R.string.game_presentation_game_preview_background_description),
                        contentScale = ContentScale.Crop,
                        placeholder = coverPainter,
                        error = coverPainter,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = coverPainter,
                        contentDescription = stringResource(R.string.game_presentation_game_preview_background_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha)),
            )
        }
    }
}
