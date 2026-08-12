package com.purpletear.game.presentation.game_play.state

import com.purpletear.sutoko.game.engine.GameEngineState

/**
 * Pure mapping from engine playback state to the UI flags the SMS screen observes.
 * Side effects (analytics, auto-advance, sounds) stay outside this mapper.
 */
object GameEngineStateUiMapper {

    fun map(current: GameUiState, engineState: GameEngineState): GameUiState = when (engineState) {
        is GameEngineState.AwaitingInput -> current.copy(
            isAwaitingInput = true,
            isAwaitingTap = false
        )

        is GameEngineState.AwaitingTap -> current.copy(
            isAwaitingTap = true,
            isAwaitingInput = false
        )

        is GameEngineState.AwaitingMangaDismissal -> current.copy(
            isMangaActive = true
        )

        is GameEngineState.AwaitingVisualNovelDismissal -> current.copy(
            isAwaitingInput = false,
            isAwaitingTap = false
        )

        is GameEngineState.Playing,
        is GameEngineState.Ready,
        is GameEngineState.Idle,
        is GameEngineState.ChapterFinished,
        is GameEngineState.Error -> current.copy(
            isAwaitingInput = false,
            isAwaitingTap = false,
            choices = emptyList(),
            isChoicesRevealed = false,
            isMangaActive = false
        )
    }
}
