package com.purpletear.aiconversation.presentation.screens.home.state

import androidx.annotation.Keep

internal sealed class PlayabilityState() {
    data object NotConnected : PlayabilityState()
    data object Loading : PlayabilityState()
    data object Playable : PlayabilityState()
    @Keep
    data class Triable(val isAd: Boolean) : PlayabilityState()
}


