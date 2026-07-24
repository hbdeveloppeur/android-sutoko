package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameFavoriteDao
import com.purpletear.game.data.local.entity.GameFavoriteEntity
import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteGamesRepositoryImpl @Inject constructor(
    private val favoriteDao: GameFavoriteDao,
) : FavoriteGamesRepository {

    override fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeIds().map { it.toSet() }.distinctUntilChanged()

    override suspend fun toggle(gameId: String) {
        assert(gameId.isNotBlank()) { "gameId must not be blank" }
        if (favoriteDao.exists(gameId)) {
            favoriteDao.deleteByGameId(gameId)
        } else {
            favoriteDao.insert(
                GameFavoriteEntity(gameId = gameId, addedAt = System.currentTimeMillis())
            )
        }
    }
}
