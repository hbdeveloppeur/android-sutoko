package com.purpletear.game.presentation.model

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Author
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
    val isOfficial: Boolean = false,
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
        isOfficial = catalog.isOfficial,
        minAppBuild = catalog.minAppBuild,
        author = catalog.author,
    )
}
