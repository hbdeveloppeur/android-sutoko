package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.UserGameProgressEntity
import kotlinx.coroutines.flow.Flow

interface UserGameProgressRepository {
    fun observe(gameId: String): Flow<UserGameProgressEntity?>
    suspend fun get(gameId: String): UserGameProgressEntity?
    suspend fun save(progress: UserGameProgressEntity)
}
