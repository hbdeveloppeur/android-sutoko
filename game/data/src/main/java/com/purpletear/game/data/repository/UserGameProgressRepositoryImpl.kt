package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.sutoko.game.model.UserGameProgressEntity
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserGameProgressRepositoryImpl @Inject constructor(
    private val userGameProgressDao: UserGameProgressDao
) : UserGameProgressRepository {

    override fun observe(gameId: String): Flow<UserGameProgressEntity> {
        return userGameProgressDao.observe(gameId)
            .map { it ?: createDefault(gameId) }
    }

    override suspend fun get(gameId: String): UserGameProgressEntity {
        return userGameProgressDao.get(gameId) ?: createDefault(gameId)
    }

    override suspend fun save(progress: UserGameProgressEntity) {
        userGameProgressDao.save(progress)
    }

    override suspend fun delete(gameId: String) {
        userGameProgressDao.delete(gameId)
    }

    private fun createDefault(gameId: String): UserGameProgressEntity =
        UserGameProgressEntity(
            gameId = gameId,
            currentChapterCode = "1a",
            normalizedChapterCode = "1a",
            heroName = ""
        )
}
