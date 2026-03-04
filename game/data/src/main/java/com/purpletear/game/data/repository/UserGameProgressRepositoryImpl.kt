package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.sutoko.game.model.UserGameProgressEntity
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserGameProgressRepositoryImpl @Inject constructor(
    private val userGameProgressDao: UserGameProgressDao
) : UserGameProgressRepository {

    override fun observe(gameId: String): Flow<UserGameProgressEntity?> {
        return userGameProgressDao.observe(gameId)
    }

    override suspend fun get(gameId: String): UserGameProgressEntity? {
        return userGameProgressDao.get(gameId)
    }

    override suspend fun save(progress: UserGameProgressEntity) {
        userGameProgressDao.save(progress)
    }
}
