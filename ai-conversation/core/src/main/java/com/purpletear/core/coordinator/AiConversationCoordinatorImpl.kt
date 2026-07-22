package com.purpletear.core.coordinator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConversationCoordinatorImpl @Inject constructor() : AiConversationCoordinator {

    private val _isMessageCoinsDialogVisible = MutableStateFlow(false)

    override val isMessageCoinsDialogVisible: StateFlow<Boolean> =
        _isMessageCoinsDialogVisible.asStateFlow()

    override fun openMessageCoinsDialog() {
        _isMessageCoinsDialogVisible.value = true
    }

    override fun closeMessageCoinsDialog() {
        _isMessageCoinsDialogVisible.value = false
    }
}
