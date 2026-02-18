package com.purpletear.aiconversation.presentation.screens.home.state

internal sealed class PlayabilityState() {
    data object NotConnected : PlayabilityState()
    data object Loading : PlayabilityState()
    data object Playable : PlayabilityState()
    data class Triable(val isAd: Boolean) : PlayabilityState()
}


