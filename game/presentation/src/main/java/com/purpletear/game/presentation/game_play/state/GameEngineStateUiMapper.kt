package com.purpletear.game.presentation.game_play.state

import com.purpletear.sutoko.game.engine.GameEngineState

/**
 * Pure mapping from engine playback state to the UI flags the SMS screen observes.
 * Side effects (analytics, auto-advance, sounds) stay outside this mapper.
 */
object GameEngineStateUiMapper {

    fun map(current: GameUiState, engineState: GameEngineState): GameUiState = when (engineState) {
        is GameEngineState.AwaitingInput -> current.copy(
            hasLoadError = false,
            isAwaitingInput = true,
            isAwaitingTap = false,
            choices = engineState.choices,
            choiceState = engineState,
            isChoicesRevealed = if (current.choiceState == engineState) {
                current.isChoicesRevealed
            } else {
                engineState.isUserInitiated
            }
        )

        is GameEngineState.AwaitingTap -> current.copy(
            hasLoadError = false,
            isAwaitingTap = true,
            isAwaitingInput = false,
            choices = emptyList(),
            choiceState = null,
            isChoicesRevealed = false
        )

        is GameEngineState.AwaitingMangaDismissal -> current.copy(
            hasLoadError = false,
            isMangaActive = true
        )

        is GameEngineState.AwaitingVisualNovelDismissal -> current.copy(
            hasLoadError = false,
            isAwaitingInput = false,
            isAwaitingTap = false
        )

        is GameEngineState.Playing,
        is GameEngineState.Ready,
        is GameEngineState.Idle,
        is GameEngineState.ChapterFinished,
        is GameEngineState.Error -> current.copy(
            hasLoadError = engineState is GameEngineState.Error,
            isAwaitingInput = false,
            isAwaitingTap = false,
            choices = emptyList(),
            choiceState = null,
            isChoicesRevealed = false,
            isMangaActive = false
        )
    }
}
