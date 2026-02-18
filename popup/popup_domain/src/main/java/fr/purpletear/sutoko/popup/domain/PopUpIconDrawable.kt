package fr.purpletear.sutoko.popup.domain

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Stable
@Keep
data class PopUpIconDrawable(
    @DrawableRes val id: Int,
) : PopUpIcon