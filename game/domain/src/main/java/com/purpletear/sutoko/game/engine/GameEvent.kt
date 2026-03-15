package com.purpletear.sutoko.game.engine

/**
 * Events emitted by the game engine to notify the UI of state changes.
 */
sealed class GameEvent {
    data class ShowTypingIndicator(
        val characterId: Int
    ) : GameEvent()

    data class ShowMessage(
        val text: String,
        val characterId: Int,
        val isMainCharacter: Boolean
    ) : GameEvent()

    data class ShowInfo(
        val text: String
    ) : GameEvent()

    data class ChangeBackground(
        val imageUrl: String
    ) : GameEvent()

    data class SendSignal(
        val action: String,
        val payload: Map<String, String> = emptyMap()
    ) : GameEvent()

    data class ChangeChapter(
        val chapterCode: String
    ) : GameEvent()
}
