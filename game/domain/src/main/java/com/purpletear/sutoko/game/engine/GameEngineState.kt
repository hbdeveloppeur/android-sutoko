package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

/**
 * States of the game engine state machine.
 */
sealed class GameEngineState {
    data object Idle : GameEngineState()

    @Keep
    data class Ready(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    @Keep
    data class Playing(
        val chapterCode: String,
        val currentNodeId: String,
    ) : GameEngineState()

    /**
     * Engine is paused waiting for player input.
     * Used for choice nodes and interactive minigames.
     */
    @Keep
    data class AwaitingInput(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    /**
     * Engine is parked on a manga page until the player opens and dismisses it.
     * Distinct from [AwaitingInput] so the choice UI is not shown. The next node is
     * resolved only when [GameEngine.resumeFromMangaPage] is called.
     */
    @Keep
    data class AwaitingMangaDismissal(
        val chapterCode: String,
        val currentNodeId: String
    ) : GameEngineState()

    @Keep
    data class ChapterFinished(
        val chapterCode: String
    ) : GameEngineState()

    @Keep
    data class Error(
        val message: String
    ) : GameEngineState()
}
