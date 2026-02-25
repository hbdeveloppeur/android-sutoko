package com.purpletear.game.presentation.states

import androidx.annotation.Keep

/**
 * Sealed class representing the different states of a game.
 */
sealed class GameState {
    data object ReadyToPlay : GameState()
    data object Idle : GameState()
    data object DownloadRequired : GameState()

    @Keep
    data class DownloadingGame(val progress: Int) : GameState()
    data object UpdateGameRequired : GameState()
    data object UpdateAppRequired : GameState()
    data object PaymentRequired : GameState()
    data object GameFinished : GameState()

    @Keep
    data class ChapterUnavailable(val number: Int, val createdAt: Long) : GameState()

    @Keep
    data class ConfirmBuy(val isLoading: Boolean = false) : GameState()
    data object ConfirmedBuy : GameState()
    data object Loading : GameState()
    data object LoadingError : GameState()
}
