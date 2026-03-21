package com.purpletear.sutoko.game.engine

/**
 * States of the game engine state machine.
 */
sealed class GameEngineState {
    data object Idle : GameEngineState()

    data class Ready(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    data class Playing(
        val chapterCode: String,
        val currentNodeId: String,
    ) : GameEngineState()

    /**
     * Engine is paused waiting for player input.
     * Used for choice nodes and interactive minigames.
     */
    data class AwaitingInput(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    data class ChapterFinished(
        val chapterCode: String
    ) : GameEngineState()

    data class Error(
        val message: String
    ) : GameEngineState()
}
