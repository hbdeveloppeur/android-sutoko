package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.entity.GameInstallationEntity
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.sutoko.game.model.GameInstallation
import com.purpletear.sutoko.game.repository.GameInstallationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GameInstallationRepository using Room database.
 */
@Singleton
class GameInstallationRepositoryImpl @Inject constructor(
    private val dao: GameInstallationDao
) : GameInstallationRepository {

    override suspend fun saveInstallation(gameId: String, version: String) {
        val entity = GameInstallationEntity(
            gameId = gameId,
            installedVersion = version,
            installedAt = System.currentTimeMillis()
        )
        dao.insert(entity)
    }

    override suspend fun getInstallation(gameId: String): GameInstallation? {
        return dao.getById(gameId)?.toDomain()
    }

    override suspend fun getInstalledVersion(gameId: String): String? {
        return dao.getInstalledVersion(gameId)
    }

    override suspend fun isInstalled(gameId: String): Boolean {
        return dao.isInstalled(gameId)
    }

    override fun observeInstallationStatus(gameId: String): Flow<Boolean> {
        return dao.observeInstallationStatus(gameId)
    }

    override fun observeInstallation(gameId: String): Flow<GameInstallation?> {
        return dao.observeById(gameId).map { it?.toDomain() }
    }

    override suspend fun removeInstallation(gameId: String) {
        dao.delete(gameId)
    }
}
