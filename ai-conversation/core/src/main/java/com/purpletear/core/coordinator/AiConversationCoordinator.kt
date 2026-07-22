package com.purpletear.core.coordinator

import kotlinx.coroutines.flow.StateFlow

interface AiConversationCoordinator {
    val isMessageCoinsDialogVisible: StateFlow<Boolean>
    fun openMessageCoinsDialog()
    fun closeMessageCoinsDialog()
}
