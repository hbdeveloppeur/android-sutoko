package com.purpletear.aiconversation.presentation.usecase

import com.purpletear.core.coordinator.AiConversationCoordinator
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveMessageCoinsDialogVisibilityUseCase @Inject constructor(
    private val aiConversationCoordinator: AiConversationCoordinator
) {
    operator fun invoke(): StateFlow<Boolean> {
        return aiConversationCoordinator.isMessageCoinsDialogVisible
    }
}
