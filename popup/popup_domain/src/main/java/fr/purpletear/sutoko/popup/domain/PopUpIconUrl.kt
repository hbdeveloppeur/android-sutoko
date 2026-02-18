package fr.purpletear.sutoko.popup.domain

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Stable
@Keep
data class PopUpIconUrl(
    val url: String,
) : PopUpIcon