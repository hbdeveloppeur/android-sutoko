package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.UserGameProgressEntity
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserGameProgressRepositoryImpl @Inject constructor(
    private val userGameProgressDao: UserGameProgressDao
) : UserGameProgressRepository {

    override fun observe(gameId: String): Flow<UserGameProgress> {
        return userGameProgressDao.observe(gameId)
            .map { it ?: createDefault(gameId) }.map { it.toDomain() }
    }

    override suspend fun get(gameId: String): UserGameProgress {
        return (userGameProgressDao.get(gameId) ?: createDefault(gameId)).toDomain()
    }

    override suspend fun save(progress: UserGameProgress) {
        val entity = UserGameProgressEntity(
            gameId = progress.gameId,
            currentChapterCode = progress.currentChapterCode,
            normalizedChapterCode = progress.normalizedChapterCode,
            heroName = progress.heroName
        )
        userGameProgressDao.save(entity)
    }

    override suspend fun delete(gameId: String) {
        userGameProgressDao.delete(gameId)
    }

    private fun createDefault(gameId: String): UserGameProgressEntity =
        UserGameProgressEntity(
            gameId = gameId,
            currentChapterCode = "1A",
            normalizedChapterCode = "1A",
            heroName = ""
        )
}
