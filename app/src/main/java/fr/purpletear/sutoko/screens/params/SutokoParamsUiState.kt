package fr.purpletear.sutoko.screens.params

import com.example.sharedelements.utils.UiText
import androidx.annotation.Keep

@Keep
data class SutokoParamsUiState(
    val isUserConnected: Boolean = false,
    val isDeleteLoading: Boolean = false,
    val isDeleteDownloadedStoriesLoading: Boolean = false,
    val isReloadLoading: Boolean = false,
    val versionText: String = "",
    val privacyPolicyUrl: String = "",
    val effect: SutokoParamsEffect? = null,
)

sealed class SutokoParamsEffect {
    @Keep
    data class OpenPrivacyPolicy(val url: String) : SutokoParamsEffect()
    data object ShareApp : SutokoParamsEffect()
    data object NavigateBack : SutokoParamsEffect()
}
