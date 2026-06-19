package com.purpletear.core.coordinator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConversationCoordinatorImpl @Inject constructor() : AiConversationCoordinator {

    private val _events = MutableSharedFlow<AiConversationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )

    override val events: Flow<AiConversationEvent> = _events.asSharedFlow()

    override fun openMessageCoinsDialog() {
        _events.tryEmit(AiConversationEvent.OpenMessageCoinsDialog)
    }

    override fun closeMessageCoinsDialog() {
        _events.tryEmit(AiConversationEvent.CloseMessageCoinsDialog)
    }
}