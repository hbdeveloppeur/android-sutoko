package com.purpletear.game.presentation.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.purpletear.sutoko.game.model.Game

/**
 * ViewModel for GameCard component.
 * Handles business logic for the GameCard UI component.
 */
class GameCardViewModel {

    /**
     * Get the URL for the game's banner image.
     *
     * @param game The game object containing the media banner information.
     * @return The URL string for the banner image, or null if not available.
     */
    fun getBannerUrl(game: Game): String? {
        return game.bannerAsset?.storagePath?.let { path ->
            "https://sutoko.com/$path"
        }
    }

    /**
     * Create an ImageRequest for the banner image.
     *
     * @param game The game object containing the media banner information.
     * @return An ImageRequest configured for the banner image, or null if not available.
     */
    @Composable
    fun createBannerImageRequest(game: Game): ImageRequest? {
        val bannerUrl = getBannerUrl(game) ?: return null
        return ImageRequest.Builder(LocalContext.current)
            .data(bannerUrl)
            .crossfade(true)
            .build()
    }

    /**
     * Create an AsyncImagePainter for the banner image.
     *
     * @param game The game object containing the media banner information.
     * @return An AsyncImagePainter configured for the banner image, or null if not available.
     */
    @Composable
    fun createBannerPainter(game: Game): coil.compose.AsyncImagePainter? {
        val imageRequest = createBannerImageRequest(game) ?: return null
        return rememberAsyncImagePainter(imageRequest)
    }
}
