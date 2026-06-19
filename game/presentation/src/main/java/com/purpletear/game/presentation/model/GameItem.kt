package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import androidx.annotation.Keep

@Keep
data class GameItem(
    val id: String,
    val title: String,
    val version: Int,
    val sku: String? = null,
    val isPurchased: Boolean = false,
    val localVersion: String? = null,
    val skuIdentifiers: List<String> = emptyList(),
    val downloadProgress: Float? = null,
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val isFree: Boolean = true,
) {
    constructor(
        catalog: GameCatalog,
        install: GameInstall?,
        isPurchased: Boolean,
        bannerUrl: String? = null,
        logoUrl: String? = null,
        downloadProgress: Float? = null
    ) : this(
        id = catalog.id,
        title = catalog.metadata.title,
        version = catalog.version,
        skuIdentifiers = catalog.skus,
        isPurchased = isPurchased,
        localVersion = install?.localVersion,
        downloadProgress = downloadProgress,
        videoUrl = catalog.videoUrl,
        imageUrl = bannerUrl,
        logoUrl = logoUrl,
        description = catalog.metadata.description,
        isFree = catalog.price == 0,
    )
}