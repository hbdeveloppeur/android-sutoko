package com.purpletear.sutoko.game.repository.game

import com.purpletear.sutoko.game.model.game.GameCatalog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing Game data.
 */
interface GameRepository {
    fun observeOfficialGames(): Flow<List<GameCatalog>>
    fun observeUserGames(): Flow<List<GameCatalog>>
    fun observeGame(id: String): Flow<GameCatalog?>

    suspend fun getDownloadLink(gameId: String, userId: String?, userToken: String?): Result<String>
    suspend fun syncOfficialGames(languageTag: String): Result<Unit>
    suspend fun syncUserGames(languageTag: String): Result<Unit>

    suspend fun searchStories(
        query: String,
        languageTag: String,
        page: Int = 1,
        limit: Int = 20,
    ): Result<List<GameCatalog>>
}
