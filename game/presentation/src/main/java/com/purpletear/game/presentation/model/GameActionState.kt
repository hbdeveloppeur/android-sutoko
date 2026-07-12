package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.Chapter

/**
 * Legacy integer IDs of the built-in Friendzoned games. These run in their own
 * dedicated activities (see app `FriendzonedGameRouter`) with no trial support,
 * so they must remain Buy-only. Keep in sync with `FriendzonedGameRouter`.
 *
 * Note: a non-null `legacyId` alone does NOT mean Friendzoned - other legacy
 * games still support the trial. Only these IDs are excluded.
 */
private val FRIENDZONED_LEGACY_IDS = setOf(159, 160, 161, 162, 163)

sealed class GameActionState {
    data object UpdateApp : GameActionState()
    data object UpdateGame : GameActionState()
    data object Download : GameActionState()
    data class Downloading(val progress: Float) : GameActionState()
    data class Purchase(
        val chapterNumber: Int,
        val showTry: Boolean,
        val price: Int,
        val isUserConnected: Boolean,
    ) : GameActionState()

    data class ConfirmPurchase(
        val isLoading: Boolean = false,
        val price: Int = 0,
    ) : GameActionState()
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
    isUserConnected: Boolean = false,
): GameActionState = when {
    isPurchasing -> GameActionState.ConfirmPurchase(
        isLoading = isPurchaseLoading,
        price = price,
    )
    isPending -> GameActionState.Pending
    downloadProgress != null -> GameActionState.Downloading(downloadProgress)
    !isFree && !isPurchased -> GameActionState.Purchase(
        chapterNumber = currentChapter?.number ?: 1,
        showTry = legacyId !in FRIENDZONED_LEGACY_IDS && (currentChapter?.number ?: 1) <= 1,
        price = price,
        isUserConnected = isUserConnected,
    )

    localVersion == null -> GameActionState.Download
    localVersion != version -> GameActionState.UpdateGame
    appBuildNumber < minAppBuild -> GameActionState.UpdateApp
    isGameFinished -> GameActionState.GameFinished
    else -> GameActionState.Play(
        chapterNumber = currentChapter?.number ?: -1,
        isChapterAvailable = currentChapter?.isAvailable ?: false,
    )
}
