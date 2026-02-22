package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for accessing Game data.
 */
interface GameRepository {
    /**
     * Get a list of all games.
     *
     * @return A Flow emitting a list of Games.
     */
    fun getOfficialGames(): Flow<Result<List<Game>>>
    /**
     * Get a list of all games.
     *
     * @return A Flow emitting a list of Games.
     */
    fun getUsersGames(): Flow<Result<List<Game>>>

    /**
     * Get a specific game by its ID.
     *
     * @param id The ID of the game to retrieve.
     * @return A Flow emitting a Result containing the requested Game.
     */
    fun getGame(id: String): Flow<Result<Game>>

    /**
     * Observe the cached games data.
     *
     * @return A StateFlow emitting the cached list of Games.
     */
    fun observeCachedOfficialGames(): StateFlow<List<Game>?>
    fun observeCachedUsersGames(): StateFlow<List<Game>?>

    /**
     * Determines whether the specified game is updatable.
     *
     * @param game The game to check for updates.
     * @return True if the game is eligible for an update; false otherwise.
     */
    suspend fun isGameUpdatable(game: Game): Flow<Result<Boolean>>

    /**
     * Checks if the specified game has local files available on the device.
     *
     * @param game The game to check for local files.
     * @return True if the game has local files; false otherwise.
     */
    suspend fun hasGameLocalFiles(game: Game): Flow<Result<Boolean>>

    suspend fun setGameVersion(game: Game): Flow<Result<Unit>>

    suspend fun removeGame(game: Game): Flow<Result<Unit>>

    fun isFriendzonedGame(game: Game): Boolean
    fun isFriendzoned1Game(game: Game): Boolean

    /**
     * Generate a download link for a game
     *
     * @param userId The ID of the user requesting the download
     * @param userToken The token of the user requesting the download
     * @param gameId The ID of the game to download
     * @return A Flow containing the Result of the operation with the download link as a String
     */
    suspend fun generateGameDownloadLink(
        userId: String,
        userToken: String,
        gameId: String
    ): Flow<Result<String>>

    /**
     * Generate a free download link for a game
     *
     * @param gameId The ID of the game to download
     * @return A Flow containing the Result of the operation with the download link as a String
     */
    suspend fun generateFreeGameDownloadLink(
        gameId: String
    ): Flow<Result<String>>
}
