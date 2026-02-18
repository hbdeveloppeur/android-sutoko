package fr.purpletear.sutoko.popup.domain

import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.compose.runtime.Stable

@Stable
@Keep
data class PopUpIconAnimation(
    @RawRes val id: Int,
    val startingFrame: Float = 0f,
    val endingFrame: Float = 1f,
    val isLooping: Boolean = true,
) : PopUpIcon
