package com.purpletear.aiconversation.presentation.usecase

import com.purpletear.core.coordinator.AiConversationCoordinator
import com.purpletear.core.coordinator.AiConversationEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMessageCoinsDialogVisibilityUseCase @Inject constructor(
    private val aiConversationCoordinator: AiConversationCoordinator
) {
    operator fun invoke(): Flow<Boolean> {
        return aiConversationCoordinator.events.map { event ->
            when (event) {
                AiConversationEvent.OpenMessageCoinsDialog -> true
                AiConversationEvent.CloseMessageCoinsDialog -> false
            }
        }
    }
}
