package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.Chapter

sealed class GameActionState {
    data object UpdateApp : GameActionState()
    data object UpdateGame : GameActionState()
    data object Download : GameActionState()
    data class Downloading(val progress: Float) : GameActionState()
    data object Purchase : GameActionState()
    data class ConfirmPurchase(val isLoading: Boolean = false) : GameActionState()
    data object GameFinished : GameActionState()
    data object Pending : GameActionState()
    data class Play(
        val chapterNumber: Int,
        val isChapterAvailable: Boolean,
    ) : GameActionState()
}

/**
 * Derives the current call-to-action state for this game from the game metadata
 * and the surrounding UI context.
 */
fun GameItem.toGameActionState(
    isPending: Boolean = false,
    isPurchasing: Boolean = false,
    isPurchaseLoading: Boolean = false,
    currentChapter: Chapter?,
    appBuildNumber: Int,
    isGameFinished: Boolean = false,
): GameActionState = when {
    isPurchasing -> GameActionState.ConfirmPurchase(isLoading = isPurchaseLoading)
    isPending -> GameActionState.Pending
    downloadProgress != null -> GameActionState.Downloading(downloadProgress)
    !isFree && !isPurchased -> GameActionState.Purchase
    localVersion == null -> GameActionState.Download
    localVersion != version -> GameActionState.UpdateGame
    appBuildNumber < minAppBuild -> GameActionState.UpdateApp
    isGameFinished -> GameActionState.GameFinished
    else -> GameActionState.Play(
        chapterNumber = currentChapter?.number ?: -1,
        isChapterAvailable = currentChapter?.isAvailable ?: false,
    )
}
