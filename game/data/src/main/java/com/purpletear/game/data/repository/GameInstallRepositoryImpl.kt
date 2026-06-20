package com.purpletear.game.data.repository

import com.purpletear.game.data.file.GameFileManager
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameInstallRepositoryImpl @Inject constructor(
    val installDao: GameInstallationDao,
    val fileManager: GameFileManager,
) : GameInstallRepository {
    private val activeDownloads = MutableStateFlow<Map<String, Float>>(emptyMap())

    override fun observeInstalls(): Flow<List<GameInstall>> =
        installDao.observeAll().map { list -> list.map { it.toDomain() } }.distinctUntilChanged()

    override fun observeInstall(gameId: String): Flow<GameInstall?> =
        installDao.observeByGameId(gameId).map { it?.toDomain() }.distinctUntilChanged()

    override fun observeDownloadProgress(gameId: String): Flow<Float?> =
        activeDownloads.map { it[gameId] }.distinctUntilChanged()

    override fun observeDownloadProgresses(): Flow<Map<String, Float>> = activeDownloads

    override fun download(
        gameId: String,
        gameDownloadUrl: String,
        gameVersion: String
    ): Flow<Float> = flow {
        assert(gameId.isNotBlank(), { "gameId must not be blank" })
        assert(gameDownloadUrl.isNotBlank(), { "gameDownloadUrl must not be blank" })
        assert(gameVersion.isNotBlank(), { "gameVersion must not be blank" })

        activeDownloads.update { downloads ->
            if (downloads.contains(gameId)) {
                throw IllegalStateException("Download already in progress")
            }
            downloads + (gameId to 0f)
        }

        emit(0f)
        val existing = installDao.observeByGameId(gameId).firstOrNull()
        installDao.upsert(GameInstallEntity(gameId = gameId, existing?.localVersion))

        var lastReported = 0f
        try {
            fileManager.downloadAndExtract(
                gameId = gameId,
                downloadUrl = gameDownloadUrl,
                onProgress = { progress ->
                    if (progress - lastReported >= 0.05f || progress >= 0.99f) {
                        activeDownloads.update { it + (gameId to progress) }
                        emit(progress)
                        lastReported = progress
                    }
                })
            installDao.markDownloaded(gameId, gameVersion)
        } catch (e: Throwable) {
            if (existing == null) {
                runCatching { installDao.deleteByGameId(gameId) }
            } else {
                runCatching { installDao.upsert(existing) }
            }
            throw e
        } finally {
            activeDownloads.update { it - gameId }
        }
        emit(1f)
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteGame(gameId: String): Result<Unit> {
        if (activeDownloads.value.containsKey(gameId)) {
            throw IllegalStateException("Download in progress")
        }

        try {
            fileManager.deleteGame(gameId)
            installDao.deleteByGameId(gameId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
}