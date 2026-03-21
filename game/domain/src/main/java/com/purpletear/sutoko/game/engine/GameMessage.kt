package com.purpletear.sutoko.game.engine

/**
 * Represents a message in the game conversation (domain model).
 * The status drives UI behavior like typing indicators.
 */
data class GameMessage(
    val id: String,
    val text: String,
    val characterId: Int,
    val status: Status = Status.SENT
) {
    /**
     * Message status for UI state management.
     * - TYPING: Message is being typed (show typing indicator)
     * - SENT: Message is displayed in the conversation
     */
    enum class Status {
        TYPING,
        SENT
    }

    val isMainCharacter: Boolean get() = characterId == 0
}
