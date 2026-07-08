package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

class FakeGameInstallRepository : GameInstallRepository {
    private val installs = mutableMapOf<String, MutableStateFlow<GameInstall?>>()
    private val downloadProgress = mutableMapOf<String, MutableStateFlow<Float?>>()
    private val deleteResults = mutableMapOf<String, Result<Unit>>()
    private val downloadFlows = mutableMapOf<String, Flow<Float>>()

    fun setInstall(gameId: String, install: GameInstall?) {
        installs.getOrPut(gameId) { MutableStateFlow(null) }.value = install
    }

    fun setDownloadProgress(gameId: String, progress: Float?) {
        downloadProgress.getOrPut(gameId) { MutableStateFlow(null) }.value = progress
    }

    fun setDeleteResult(gameId: String, result: Result<Unit>) {
        deleteResults[gameId] = result
    }

    fun setDownloadFlow(gameId: String, flow: Flow<Float>) {
        downloadFlows[gameId] = flow
    }

    override fun observeInstall(gameId: String): Flow<GameInstall?> {
        return installs.getOrPut(gameId) { MutableStateFlow(null) }.asStateFlow()
    }

    override fun observeDownloadProgress(gameId: String): Flow<Float?> {
        return downloadProgress.getOrPut(gameId) { MutableStateFlow(null) }.asStateFlow()
    }

    override suspend fun deleteGame(gameId: String, legacyId: Int?): Result<Unit> {
        return deleteResults[gameId] ?: Result.success(Unit)
    }

    override fun download(
        gameId: String,
        gameDownloadUrl: String,
        gameVersion: String,
        legacyId: Int?
    ): Flow<Float> {
        return downloadFlows[gameId] ?: emptyFlow()
    }

    override fun observeInstalls(): Flow<List<GameInstall>> = emptyFlow()
    override fun observeDownloadProgresses(): Flow<Map<String, Float>> = emptyFlow()
    override fun cancelDownload(gameId: String) {}
    override suspend fun ensureBuiltInGamesInstalled(catalogs: List<GameCatalog>) {}
}
