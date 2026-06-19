package com.purpletear.aiconversation.presentation.usecase

import com.purpletear.core.coordinator.AiConversationCoordinator
import javax.inject.Inject

class CloseMessagesCoinsDialogUseCase @Inject constructor(
    private val aiConversationCoordinator: AiConversationCoordinator
) {
    operator fun invoke() {
        aiConversationCoordinator.closeMessageCoinsDialog()
    }
}
