package com.purpletear.game.presentation.game_play.state

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.scene.Scene
import androidx.annotation.Keep

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
    val currentScene: Scene? = null,
    val characters: Map<Int, Character> = emptyMap(),
    val currentVocalUrl: String? = null,
    val isVocalPlaying: Boolean = false,
    val vocalProgress: Float = 0f,
    val errorMessage: String? = null,
)
