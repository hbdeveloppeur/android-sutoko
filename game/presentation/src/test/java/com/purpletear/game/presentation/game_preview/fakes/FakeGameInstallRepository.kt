package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameDownloadRequest
import com.purpletear.sutoko.game.model.game.GameDownloadState
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CancellationException

class FakeGameInstallRepository : GameInstallRepository {
    private val installs = mutableMapOf<String, MutableStateFlow<GameInstall?>>()
    private val downloadProgress = mutableMapOf<String, MutableStateFlow<Float?>>()
    private val downloadStates = mutableMapOf<String, MutableStateFlow<GameDownloadState?>>()
    private val deleteResults = mutableMapOf<String, Result<Unit>>()
    private val downloadFlows = mutableMapOf<String, Flow<Float>>()
    val deletions = mutableListOf<Pair<String, Int?>>()
    var downloadCalls = 0
        private set

    fun setInstall(gameId: String, install: GameInstall?) {
        installs.getOrPut(gameId) { MutableStateFlow(null) }.value = install
    }

    fun setDownloadProgress(gameId: String, progress: Float?) {
        downloadProgress.getOrPut(gameId) { MutableStateFlow(null) }.value = progress
        setDownloadState(gameId, progress?.let { GameDownloadState.Downloading(it) })
    }

    fun setDownloadState(gameId: String, state: GameDownloadState?) {
        downloadStates.getOrPut(gameId) { MutableStateFlow(null) }.value = state
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

    override fun observeDownloadState(gameId: String): Flow<GameDownloadState?> =
        downloadStates.getOrPut(gameId) { MutableStateFlow(null) }.asStateFlow()

    override suspend fun deleteGame(gameId: String, legacyId: Int?): Result<Unit> {
        deletions += gameId to legacyId
        return (deleteResults[gameId] ?: Result.success(Unit)).also { result ->
            if (result.isSuccess) {
                setInstall(gameId, null)
                setDownloadState(gameId, null)
            }
        }
    }

    override fun download(
        gameId: String,
        gameDownloadUrl: String,
        gameVersion: String,
        legacyId: Int?
    ): Flow<Float> {
        downloadCalls++
        return downloadFlows[gameId] ?: emptyFlow()
    }

    override fun download(
        gameId: String,
        resolveRequest: suspend () -> GameDownloadRequest,
    ): Flow<Float> = flow {
        setDownloadState(gameId, GameDownloadState.Preparing)
        try {
            val request = resolveRequest()
            download(gameId, request.url, request.version, request.legacyId).collect { progress ->
                setDownloadProgress(gameId, progress)
                emit(progress)
            }
            setInstall(gameId, GameInstall(gameId, request.version.toInt()))
            setDownloadState(gameId, GameDownloadState.Completed(request.version.toInt()))
        } catch (error: CancellationException) {
            setDownloadState(gameId, GameDownloadState.Cancelled)
            throw error
        } catch (error: Throwable) {
            setDownloadState(gameId, GameDownloadState.Failed)
            throw error
        } finally {
            downloadProgress.getOrPut(gameId) { MutableStateFlow(null) }.value = null
        }
    }

    override fun observeInstalls(): Flow<List<GameInstall>> = emptyFlow()
    override fun observeDownloadProgresses(): Flow<Map<String, Float>> = emptyFlow()
    override fun cancelDownload(gameId: String) {}
    override suspend fun ensureBuiltInGamesInstalled(catalogs: List<GameCatalog>) {}
}
