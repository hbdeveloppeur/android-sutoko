package com.purpletear.ai_conversation.ui.sealed

sealed class NavigationEvent {
    data object NavigateBack : NavigationEvent()
}