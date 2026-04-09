package com.purpletear.game.presentation.game_play.state

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.scene.Scene

/**
 * UI state for the game play screen.
 * Represents the current state of the game session including messages, choices, and input status.
 */
data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val messages: List<GameMessage> = emptyList(),
    val choices: List<HandlerEffect.ShowChoices.Choice> = emptyList(),
    val isAwaitingInput: Boolean = false,
    val currentScene: Scene? = null
)
