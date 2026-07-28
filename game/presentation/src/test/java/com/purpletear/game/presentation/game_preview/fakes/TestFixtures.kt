package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.model.game.GameMetadata

object TestFixtures {
    const val GAME_ID = "game-1"
    const val LEGACY_ID = 42

    fun gameCatalog(
        id: String = GAME_ID,
        version: Int = 1,
        price: Int = 0,
        skus: List<String> = emptyList(),
        userNickNameRequired: Boolean = false,
        canvasTechnologyRequiredVersion: Int = 1,
        isOfficial: Boolean = true,
        legacyId: Int? = LEGACY_ID,
    ): GameCatalog = GameCatalog(
        id = id,
        version = version,
        price = price,
        skus = skus,
        metadata = GameMetadata(title = "Test Game", description = "A test game"),
        legacyId = legacyId,
        userNickNameRequired = userNickNameRequired,
        canvasTechnologyRequiredVersion = canvasTechnologyRequiredVersion,
        isOfficial = isOfficial,
        banner = asset(storagePath = "banner/$id"),
        logo = asset(storagePath = "logo/$id"),
        menuBackground = asset(storagePath = "background/$id"),
    )

    fun gameInstall(
        gameId: String = GAME_ID,
        localVersion: Int? = null,
    ): GameInstall = GameInstall(
        gameId = gameId,
        localVersion = localVersion,
    )

    private fun asset(storagePath: String): Asset = Asset(
        id = 0L,
        originalFilename = "",
        width = 0,
        height = 0,
        createdAt = 0L,
        fileSizeBytes = 0,
        mimeType = "",
        storagePath = storagePath,
        thumbnailStoragePath = "",
    )
}
