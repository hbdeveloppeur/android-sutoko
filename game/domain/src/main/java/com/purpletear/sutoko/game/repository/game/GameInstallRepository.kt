package com.purpletear.sutoko.game.repository.game

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import kotlinx.coroutines.flow.Flow

interface GameInstallRepository {
    fun observeInstalls(): Flow<List<GameInstall>>
    fun observeInstall(gameId: String): Flow<GameInstall?>
    fun download(
        gameId: String,
        gameDownloadUrl: String,
        gameVersion: String,
        legacyId: Int? = null,
    ): Flow<Float>

    fun observeDownloadProgress(gameId: String): Flow<Float?>
    fun observeDownloadProgresses(): Flow<Map<String, Float>>
    suspend fun deleteGame(gameId: String, legacyId: Int? = null): Result<Unit>
    fun cancelDownload(gameId: String)

    /**
     * Ensures that built-in games are marked as installed so the UI does not
     * prompt the user to download content that is already bundled in the APK.
     *
     * @param catalogs The current catalog snapshot to sync versions against.
     */
    suspend fun ensureBuiltInGamesInstalled(catalogs: List<GameCatalog>)
}
