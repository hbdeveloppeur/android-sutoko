package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

/**
 * Conversation display mode for the game engine.
 * Controls how messages are rendered and whether typing indicators are shown.
 */
@Keep
enum class ConversationMode {
    /**
     * SMS-style chat interface.
     * Shows typing indicators and delays between messages.
     */
    SMS,

    /**
     * In-Real-Life dialogue mode.
     * No typing indicators, messages display immediately.
     * Typically used for cutscenes or direct dialogue.
     */
    IRL,
}
