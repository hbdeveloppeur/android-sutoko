package com.purpletear.game_presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game_presentation.viewmodels.GameBackgroundPreviewMediaViewModel
import com.purpletear.sutoko.game.model.Game

/**
 * A composable that displays a background media for a game preview.
 * It handles the logic for getting the image URL and displays it.
 * It adapts the video URL based on screen width.
 *
 * @param game The game object containing media information
 * @param modifier The modifier to be applied to the component
 * @param viewModel The ViewModel that provides the image URL logic
 */
@Composable
internal fun GameBackgroundPreviewMedia(
    game: Game,
    modifier: Modifier = Modifier,
    viewModel: GameBackgroundPreviewMediaViewModel = hiltViewModel()
) {
    // Get the screen width 
    val context = LocalContext.current

    // Determine if video is present
    val hasVideo = game.videoUrl != null

    // Animation for fading out the image when video is present
    val imageAlpha by animateFloatAsState(
        targetValue = if (hasVideo) 0f else 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "imageAlpha"
    )

    // Display the video using BackgroundMedia
    game.videoUrl?.let { videoUrl ->
        BackgroundMedia(
            videoUrl = videoUrl,
            modifier = Modifier.fillMaxSize()
        )
    }

    viewModel.getImagePreviewBackgroundLink(game = game)?.let { backgroundImage ->
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(backgroundImage)
                .crossfade(true)
                .build(),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha)
        )
    }

    Box(Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.1f)))
}
