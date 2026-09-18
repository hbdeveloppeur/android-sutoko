package com.purpletear.game.presentation.game_play

import com.purpletear.game.presentation.game_play.state.GameUiState

internal enum class GameContentReadiness { Waiting, Ready, Failed }

internal fun GameUiState.contentReadiness(sceneReady: Boolean = true): GameContentReadiness = when {
    hasLoadError -> GameContentReadiness.Failed
    !sceneReady -> GameContentReadiness.Waiting
    currentScene != null || messages.isNotEmpty() || choices.isNotEmpty() || isAwaitingInput || isAwaitingTap ||
        visualNovel != null || fakeNotification != null -> GameContentReadiness.Ready
    else -> GameContentReadiness.Waiting
}
