package com.purpletear.game_presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.game.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the GameCard component.
 * Injects HostProvider to access the host name for API requests.
 */
@HiltViewModel
class GameCardViewModel @Inject constructor(
    private val hostProvider: HostProvider
) : ViewModel() {

    fun getImageBannerLink(game: Game): String {
        return game.mediaMainBanner?.filename?.let { filename ->
            hostProvider.getPublicMedia(filename = filename)
        } ?: ""
    }
}
