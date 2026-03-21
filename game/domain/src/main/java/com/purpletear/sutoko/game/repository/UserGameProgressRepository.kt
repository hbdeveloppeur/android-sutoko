package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.UserGameProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user game progress.
 * Provides non-nullable results - returns default progress if none exists.
 */
interface UserGameProgressRepository {
    /**
     * Observes progress for a game. Emits default progress if none exists.
     */
    fun observe(gameId: String): Flow<UserGameProgressEntity>

    /**
     * Gets progress for a game. Returns default progress if none exists.
     */
    suspend fun get(gameId: String): UserGameProgressEntity

    /**
     * Saves progress for a game.
     */
    suspend fun save(progress: UserGameProgressEntity)

    /**
     * Deletees progress for a game.
     */
    suspend fun delete(gameId: String)
}
