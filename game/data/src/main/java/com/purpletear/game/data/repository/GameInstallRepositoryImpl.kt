package com.purpletear.game.data.repository

import com.purpletear.game.data.file.GameFileManager
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.sutoko.game.exception.DownloadAlreadyInProgressException
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameDownloadRequest
import com.purpletear.sutoko.game.model.game.GameDownloadState
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import purpletear.fr.purpleteartools.GlobalData
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameInstallRepositoryImpl @Inject constructor(
    val installDao: GameInstallationDao,
    val fileManager: GameFileManager,
) : GameInstallRepository {
    private val activeDownloads = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val downloadStates = MutableStateFlow<Map<String, GameDownloadState>>(emptyMap())
    private val activeProducers = ConcurrentHashMap<String, Job>()
    private val activeInstallActions = ConcurrentHashMap.newKeySet<String>()

    override fun observeInstalls(): Flow<List<GameInstall>> =
        installDao.observeAll().map { list -> list.map { it.toDomain() } }.distinctUntilChanged()

    override fun observeInstall(gameId: String): Flow<GameInstall?> =
        installDao.observeByGameId(gameId).map { it?.toDomain() }.distinctUntilChanged()

    override fun observeDownloadProgress(gameId: String): Flow<Float?> =
        activeDownloads.map { it[gameId] }.distinctUntilChanged()

    override fun observeDownloadProgresses(): Flow<Map<String, Float>> = activeDownloads

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDownloadState(gameId: String): Flow<GameDownloadState?> =
        downloadStates.map { it[gameId] }.distinctUntilChanged().flatMapLatest { state ->
            if (state is GameDownloadState.Completed) {
                // This fresh observation starts after the installation commit. Later repair or
                // catalog writes must invalidate the retained version instead of resurrecting it.
                installDao.observeByGameId(gameId).map { install ->
                    state.takeIf { install?.localVersion == state.version }
                }
            } else {
                flowOf(state)
            }
        }.distinctUntilChanged()

    override fun download(
        gameId: String,
        gameDownloadUrl: String,
        gameVersion: String,
        legacyId: Int?,
    ): Flow<Float> = download(gameId) {
        GameDownloadRequest(gameDownloadUrl, gameVersion, legacyId)
    }

    override fun download(
        gameId: String,
        resolveRequest: suspend () -> GameDownloadRequest,
    ): Flow<Float> = channelFlow {
        assert(gameId.isNotBlank(), { "gameId must not be blank" })

        if (!activeInstallActions.add(gameId)) {
            throw DownloadAlreadyInProgressException("Installation change already in progress: $gameId")
        }

        try {
            activeProducers[gameId] = coroutineContext[Job]
                ?: error("download() must be collected inside a CoroutineScope")
            downloadStates.update { it + (gameId to GameDownloadState.Preparing) }
            activeDownloads.update { it + (gameId to 0f) }
            send(0f)
            val request = resolveRequest()
            require(request.url.isNotBlank()) { "download URL must not be blank" }
            val version = request.version.toInt()
            val existing = installDao.observeByGameId(gameId).firstOrNull()
            try {
                installDao.upsert(GameInstallEntity(gameId = gameId, existing?.localVersion))
                var lastReportedPercent: Int? = null
                var lastState: GameDownloadState = GameDownloadState.Preparing
                fileManager.downloadAndExtract(
                    gameId = gameId,
                    downloadUrl = request.url,
                    onState = { state ->
                        val fraction = (state as? GameDownloadState.Downloading)?.progress
                        val percent = fraction?.let { (it * 100).toInt() }
                        val changedPhase = state::class != lastState::class
                        if (changedPhase || percent != lastReportedPercent) {
                            downloadStates.update { it + (gameId to state) }
                            val legacyProgress = when (state) {
                                GameDownloadState.Installing -> 0.99f
                                is GameDownloadState.Downloading -> fraction?.coerceAtMost(0.99f) ?: 0f
                                else -> error("Unexpected file download state: $state")
                            }
                            if (activeDownloads.value[gameId] != legacyProgress) {
                                activeDownloads.update { it + (gameId to legacyProgress) }
                                send(legacyProgress)
                            }
                            lastReportedPercent = percent
                            lastState = state
                        }
                    },
                    legacyId = request.legacyId,
                )
                installDao.markDownloaded(gameId, request.version)
                downloadStates.update { it + (gameId to GameDownloadState.Completed(version)) }
            } catch (e: Throwable) {
                withContext(NonCancellable) {
                    if (existing == null) {
                        runCatching { installDao.deleteByGameId(gameId) }
                    } else {
                        runCatching { installDao.upsert(existing) }
                    }
                }
                throw e
            }
        } catch (e: CancellationException) {
            downloadStates.update { it + (gameId to GameDownloadState.Cancelled) }
            throw e
        } catch (e: Throwable) {
            downloadStates.update { it + (gameId to GameDownloadState.Failed) }
            throw e
        } finally {
            activeProducers.remove(gameId)
            activeDownloads.update { it - gameId }
            activeInstallActions.remove(gameId)
        }
        send(1f)
    }.flowOn(Dispatchers.IO)

    override fun cancelDownload(gameId: String) {
        activeProducers[gameId]?.cancel()
    }

    override suspend fun deleteGame(gameId: String, legacyId: Int?): Result<Unit> {
        currentCoroutineContext().ensureActive()
        if (!activeInstallActions.add(gameId)) {
            return Result.failure(IllegalStateException("Installation change in progress"))
        }

        try {
            // Once files are removed, cancellation must not retain an installed database row.
            withContext(NonCancellable) {
                fileManager.deleteGame(gameId, legacyId)
                installDao.deleteByGameId(gameId)
                downloadStates.update { it - gameId }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return Result.failure(e)
        } finally {
            activeInstallActions.remove(gameId)
        }
        return Result.success(Unit)
    }

    override suspend fun ensureBuiltInGamesInstalled(catalogs: List<GameCatalog>) {
        val builtInLegacyIds = setOf(GlobalData.Game.FRIENDZONE.id)

        catalogs
            .filter { it.legacyId in builtInLegacyIds }
            .forEach { catalog ->
                installDao.upsert(
                    GameInstallEntity(
                        gameId = catalog.id,
                        localVersion = catalog.version,
                    )
                )
            }
    }
}
