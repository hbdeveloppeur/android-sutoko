package com.purpletear.game.presentation.smsgame.engine

import androidx.annotation.Keep

/**
 * Sealed class representing events emitted during game execution.
 */
@Keep
sealed class GameEvent {
    data class ShowMessage(
        val text: String,
        val characterId: Int,
        val isMainCharacter: Boolean,
        val delayMs: Long
    ) : GameEvent()

    data class ShowChoices(
        val options: List<String>,
        val onSelect: (Int) -> Unit
    ) : GameEvent()

    data class ShowInfo(
        val text: String
    ) : GameEvent()

    data class ChangeBackground(
        val imageUrl: String
    ) : GameEvent()

    data class UnlockTrophy(
        val trophyId: String
    ) : GameEvent()

    data class SendSignal(
        val action: String,
        val payload: Map<String, String>
    ) : GameEvent()

    data class ChangeChapter(
        val chapterCode: String
    ) : GameEvent()

    data object WaitingForInput : GameEvent()
}
