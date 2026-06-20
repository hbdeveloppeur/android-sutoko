package com.purpletear.game.presentation.model

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Author
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall

@Keep
data class GameItem(
    val id: String,
    val title: String,
    val version: Int,
    val sku: String? = null,
    val isPurchased: Boolean = false,
    val localVersion: Int? = null,
    val skuIdentifiers: List<String> = emptyList(),
    val downloadProgress: Float? = null,
    val videoUrl: String? = null,
    val menuBackgroundUrl: String? = null,
    val imageUrl: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val isFree: Boolean = true,
    val minAppBuild: Int,
    val author: Author? = null,
) {
    constructor(
        catalog: GameCatalog,
        install: GameInstall?,
        isPurchased: Boolean,
        bannerUrl: String? = null,
        logoUrl: String? = null,
        menuBackgroundUrl: String? = null,
        downloadProgress: Float? = null
    ) : this(
        id = catalog.id,
        title = catalog.metadata.title,
        version = catalog.version,
        skuIdentifiers = catalog.skus,
        isPurchased = isPurchased,
        localVersion = install?.localVersion,
        downloadProgress = downloadProgress,
        menuBackgroundUrl = menuBackgroundUrl,
        videoUrl = catalog.videoUrl,
        imageUrl = bannerUrl,
        logoUrl = logoUrl,
        description = catalog.metadata.description,
        isFree = catalog.price == 0,
        minAppBuild = catalog.minAppBuild,
        author = catalog.author,
    )
}

sealed class GameAction {
    object UpdateApp : GameAction()
    object UpdateGame : GameAction()
    object Download : GameAction()
    data class Downloading(val progress: Float) : GameAction()
    object Purchase : GameAction()
    data class ConfirmPurchase(val isBought: Boolean) : GameAction()
    object GameFinished : GameAction()
    object Pending : GameAction()
    data class Play(val chapterNumber: Int, val isChapterAvailable: Boolean) : GameAction()
}

fun GameItem.toGameAction(
    isPending: Boolean,
    isPurchasing: Boolean,
    currentChapter: Chapter?,
    appBuildNumber: Int,
    isGameFinished: Boolean,
): GameAction {
    return when {
        isPurchasing -> GameAction.ConfirmPurchase(false)
        isPending -> GameAction.Pending
        downloadProgress != null -> GameAction.Downloading(downloadProgress)
        !isFree && !isPurchased -> GameAction.Purchase
        localVersion == null -> GameAction.Download
        localVersion != version -> GameAction.UpdateGame
        appBuildNumber > minAppBuild -> GameAction.UpdateApp
        isGameFinished -> GameAction.GameFinished
        else -> GameAction.Play(currentChapter?.number ?: -1, currentChapter?.isAvailable ?: false)
    }
}