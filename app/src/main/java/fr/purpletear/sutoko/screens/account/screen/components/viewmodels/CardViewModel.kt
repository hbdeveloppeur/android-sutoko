package fr.purpletear.sutoko.screens.account.screen.components.viewmodels

import androidx.lifecycle.ViewModel
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.getThumbnailUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the CardView component.
 */
@HiltViewModel
class CardViewModel @Inject constructor(
    private val hostProvider: HostProvider
) : ViewModel() {

    fun getImageSquareLogo(game: Game): String {
        return game.logoAsset.getThumbnailUrl() ?: ""
    }
}
