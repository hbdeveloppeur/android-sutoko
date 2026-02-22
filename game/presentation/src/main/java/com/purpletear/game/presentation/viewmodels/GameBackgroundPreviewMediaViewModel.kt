package com.purpletear.game.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.game.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the GameBackgroundPreviewMedia component.
 * Injects HostProvider to access the host name for API requests.
 */
@HiltViewModel
class GameBackgroundPreviewMediaViewModel @Inject constructor(
    private val hostProvider: HostProvider,
) : ViewModel() {

    /**
     * Gets the image URL for the game preview background.
     * Uses bannerAsset's storagePath to generate the URL.
     *
     * @param game The game object containing media information
     * @return The URL of the background image or null if no image is available
     */
    fun getImagePreviewBackgroundLink(game: Game): String? {
        return game.bannerAsset?.storagePath?.let { path ->
            "https://sutoko.com/$path"
        }
    }
}