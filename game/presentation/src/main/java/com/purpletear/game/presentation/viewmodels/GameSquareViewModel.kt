package com.purpletear.game.presentation.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.purpletear.sutoko.game.model.Game

/**
 * ViewModel for GameSquare component.
 * Handles business logic for the GameSquare UI component.
 */
class GameSquareViewModel {

    /**
     * Get the URL for the game's logo image.
     *
     * @param game The game object containing the media logo information.
     * @return The URL string for the logo image, or null if not available.
     */
    fun getLogoUrl(game: Game): String? {
        return game.logoAsset?.storagePath?.let { path ->
            "https://sutoko.com/$path"
        }
    }

    /**
     * Create an ImageRequest for the logo image.
     *
     * @param game The game object containing the media logo information.
     * @return An ImageRequest configured for the logo image, or null if not available.
     */
    @Composable
    fun createLogoImageRequest(game: Game): ImageRequest? {
        val logoUrl = getLogoUrl(game) ?: return null
        return ImageRequest.Builder(LocalContext.current)
            .data(logoUrl)
            .crossfade(true)
            .build()
    }

    /**
     * Create an AsyncImagePainter for the logo image.
     *
     * @param game The game object containing the media logo information.
     * @return An AsyncImagePainter configured for the logo image, or null if not available.
     */
    @Composable
    fun createLogoPainter(game: Game): coil.compose.AsyncImagePainter? {
        val imageRequest = createLogoImageRequest(game) ?: return null
        return rememberAsyncImagePainter(imageRequest)
    }
}
