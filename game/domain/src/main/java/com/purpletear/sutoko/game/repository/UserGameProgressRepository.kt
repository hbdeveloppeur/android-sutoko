package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.UserGameProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user game progress.
 * Provides non-nullable results - returns default progress if none exists.
 */
interface UserGameProgressRepository {
    /**
     * Observes progress for a game. Emits default progress if none exists.
     */
    fun observe(gameId: String): Flow<UserGameProgress>

    /**
     * Gets progress for a game. Returns default progress if none exists.
     */
    suspend fun get(gameId: String): UserGameProgress

    /**
     * Saves progress for a game.
     */
    suspend fun save(progress: UserGameProgress)

    /**
     * Deletees progress for a game.
     */
    suspend fun delete(gameId: String)
}
