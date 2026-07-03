package com.purpletear.game.presentation.game_play.state

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.scene.Scene

/**
 * UI state for the live-update indicator shown during real-time story testing.
 */
@Keep
sealed class LiveUpdateStatus {
    @Keep
    data object Loading : LiveUpdateStatus()

    @Keep
    data object Connected : LiveUpdateStatus()

    @Keep
    data object Disconnected : LiveUpdateStatus()
}

/**
 * UI state for the game play screen.
 * Represents the current state of the game session including messages, choices, and input status.
 */
@Keep
data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val messages: List<GameMessage> = emptyList(),
    val choices: List<HandlerEffect.ShowChoices.Choice> = emptyList(),
    val isAwaitingInput: Boolean = false,
    val isChoicesRevealed: Boolean = false,
    val currentScene: Scene? = null,
    val characters: Map<Int, Character> = emptyMap(),
    val currentVocalUrl: String? = null,
    val isVocalPlaying: Boolean = false,
    val vocalProgress: Float = 0f,
    val isLoadingStoryUpdates: Boolean = false,
    val liveUpdateStatus: LiveUpdateStatus? = null,
    val hasPendingStoryUpdate: Boolean = false,
    val isLiveUpdateMode: Boolean = false,
    val showNextChapterButton: Boolean = true,
    @StringRes val nextChapterTitleRes: Int? = null,
)
