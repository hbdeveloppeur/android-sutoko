package com.purpletear.game.presentation.smsgame.engine

import androidx.annotation.Keep

/**
 * Sealed class representing the state of the game engine.
 */
@Keep
sealed class GameEngineState {
    object Idle : GameEngineState()

    data class Ready(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    data class Playing(
        val chapterCode: String,
        val currentNodeId: String,
        val messages: List<MessageItem>
    ) : GameEngineState()

    data class WaitingInput(
        val chapterCode: String,
        val currentNodeId: String,
        val messages: List<MessageItem>
    ) : GameEngineState()

    data class Completed(
        val chapterCode: String
    ) : GameEngineState()

    data class Error(
        val message: String
    ) : GameEngineState()
}
