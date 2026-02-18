package com.purpletear.ai_conversation.ui.screens.media.image_generator.state

sealed class ImageGeneratorState {
    data object NotConnected : ImageGeneratorState()
    data object Loading : ImageGeneratorState()
    data object InsufficientCoins : ImageGeneratorState()
    data object Success : ImageGeneratorState()
}