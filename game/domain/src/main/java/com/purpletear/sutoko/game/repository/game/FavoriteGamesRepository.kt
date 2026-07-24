package com.purpletear.sutoko.game.repository.game

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the user's favorite stories.
 */
interface FavoriteGamesRepository {
    fun observeFavoriteIds(): Flow<Set<String>>

    /**
     * Adds [gameId] to favorites when absent, removes it otherwise.
     */
    suspend fun toggle(gameId: String)
}
