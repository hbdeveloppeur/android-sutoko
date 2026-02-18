package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.states

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Keep
@Stable
data class ChatConversationState constructor(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)
