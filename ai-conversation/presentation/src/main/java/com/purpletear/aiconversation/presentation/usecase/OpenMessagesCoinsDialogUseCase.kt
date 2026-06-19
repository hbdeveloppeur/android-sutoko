package com.purpletear.aiconversation.presentation.usecase

import com.purpletear.core.coordinator.AiConversationCoordinator
import javax.inject.Inject

class OpenMessagesCoinsDialogUseCase @Inject constructor(
    private val aiConversationCoordinator: AiConversationCoordinator
) {
    operator fun invoke() {
        aiConversationCoordinator.openMessageCoinsDialog()
    }
}
