package com.purpletear.core.coordinator

import kotlinx.coroutines.flow.Flow

interface AiConversationCoordinator {
    val events: Flow<AiConversationEvent>
    fun openMessageCoinsDialog()
    fun closeMessageCoinsDialog()
}

sealed interface AiConversationEvent {
    object OpenMessageCoinsDialog : AiConversationEvent
    object CloseMessageCoinsDialog : AiConversationEvent
}