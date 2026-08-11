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
     * Engine is parked waiting for the player to tap the screen to advance.
     * Used for tap-to-continue pacing in narrative nodes.
     *
     * [autoAdvanceAfterMs] is the delay after which the driver (UI layer) is expected
     * to advance on its own, so the story progresses without requiring a tap.
     *
     * [requiresTap] mirrors [HandlerCommand.AwaitTap.requiresTap]: when false the gate
     * auto-resolves after [autoAdvanceAfterMs] even in click-to-advance mode.
     */
    @Keep
    data class AwaitingTap(
        val chapterCode: String,
        val currentNodeId: String,
        val autoAdvanceAfterMs: Long,
        val requiresTap: Boolean = true
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

    /**
     * Engine is parked on a visual novel overlay until the player dismisses it.
     * Distinct from [AwaitingInput] so the choice UI is not shown. The next node is
     * resolved only when [GameEngine.resumeFromVisualNovel] is called.
     */
    @Keep
    data class AwaitingVisualNovelDismissal(
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
