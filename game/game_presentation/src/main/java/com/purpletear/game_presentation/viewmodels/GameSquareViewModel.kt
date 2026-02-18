package com.purpletear.game_presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.game.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the GameSquare component.
 * Injects HostProvider to access the host name for API requests.
 */
@HiltViewModel
class GameSquareViewModel @Inject constructor(
    private val hostProvider: HostProvider
) : ViewModel() {

    fun getImageSquareLink(game: Game): String {
        return game.mediaLogoSquare?.filename?.let { filename ->
            hostProvider.getPublicMedia(filename = filename)
        } ?: ""
    }
}
