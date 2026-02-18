package com.purpletear.aiconversation.presentation.sealed

sealed class NavigationEvent {
    data object NavigateBack : NavigationEvent()
}