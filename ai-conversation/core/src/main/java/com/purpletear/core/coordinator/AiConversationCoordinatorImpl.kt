package com.purpletear.core.coordinator

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConversationCoordinatorImpl @Inject constructor() : AiConversationCoordinator {

    // DROP_OLDEST: tryEmit never fails, a stale event is dropped instead of the newest one.
    private val _events = MutableSharedFlow<AiConversationEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<AiConversationEvent> = _events.asSharedFlow()

    override fun openMessageCoinsDialog() {
        _events.tryEmit(AiConversationEvent.OpenMessageCoinsDialog)
    }

    override fun closeMessageCoinsDialog() {
        _events.tryEmit(AiConversationEvent.CloseMessageCoinsDialog)
    }
}