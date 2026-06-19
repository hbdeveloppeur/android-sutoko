package com.purpletear.game.presentation.game_catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.getThumbnailUrl

/**
 * ViewModel for GameSquare component.
 * Handles business logic for the GameSquare UI component.
 */
class GameSquareViewModel {

    /**
     * Get the URL for the game's logo image.
     *
     * @param GameCatalog The game object containing the media logo information.
     * @return The URL string for the logo image, or null if not available.
     */
    fun getLogoUrl(gameCatalog: GameCatalog): String? {
        return gameCatalog.logo.getThumbnailUrl()
    }

    /**
     * Create an ImageRequest for the logo image.
     *
     * @param gameCatalog The game object containing the media logo information.
     * @return An ImageRequest configured for the logo image, or null if not available.
     */
    @Composable
    fun createLogoImageRequest(gameCatalog: GameCatalog): ImageRequest? {
        val logoUrl = getLogoUrl(gameCatalog) ?: return null
        return ImageRequest.Builder(LocalContext.current)
            .data(logoUrl)
            .crossfade(true)
            .build()
    }

    /**
     * Create an AsyncImagePainter for the logo image.
     *
     * @param GameCatalog The game object containing the media logo information.
     * @return An AsyncImagePainter configured for the logo image, or null if not available.
     */
    @Composable
    fun createLogoPainter(gameCatalog: GameCatalog): AsyncImagePainter? {
        val imageRequest = createLogoImageRequest(gameCatalog) ?: return null
        return rememberAsyncImagePainter(imageRequest)
    }
}