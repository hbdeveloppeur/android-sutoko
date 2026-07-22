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

    /**
     * Re-fetch a single story from the network and upsert it locally, preserving
     * its official/user categorization. Used to detect new versions of one story.
     */
    suspend fun syncGame(gameId: String, languageTag: String): Result<Unit>

    /**
     * Load the next page of user-created games.
     *
     * @param languageTag The BCP-47 language tag to request games for.
     * @return A Result containing `true` if more pages remain, `false` if the loaded page was the last one.
     */
    suspend fun loadMoreUserGames(languageTag: String): Result<Boolean>

    suspend fun searchStories(
        query: String,
        languageTag: String,
        page: Int = 1,
        limit: Int = 20,
    ): Result<List<GameCatalog>>

    suspend fun getOneUserGames(
        userId: String,
        page: Int,
        limit: Int,
    ): Result<List<GameCatalog>>
}
