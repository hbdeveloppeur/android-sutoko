package com.purpletear.game_data.repository

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.game_data.remote.GameApi
import com.purpletear.game_data.remote.dto.DownloadLinkRequestDto
import com.purpletear.game_data.remote.dto.FreeDownloadLinkRequestDto
import com.purpletear.game_data.remote.dto.toDomain
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

/**
 * Implementation of the GameRepository interface.
 */
class GameRepositoryImpl @Inject constructor(
    private val api: GameApi,
    private val tableOfSymbols: TableOfSymbols,
    private val context: Context,
) : GameRepository {

    companion object {
        // Minimum request duration in milliseconds
        private const val MIN_REQUEST_DURATION = 1000L
    }

    // Thread-safe and observable cache
    private val gamesStateFlow = MutableStateFlow<List<Game>?>(null)

    /**
     * Get a list of all games.
     *
     * @return A Flow emitting a Result containing a list of Games.
     */
    override fun getGames(): Flow<Result<List<Game>>> = flow {
        try {
            // Return cached value if available
            gamesStateFlow.value?.let {
                emit(Result.success(it))
            } ?: run {
                val startTime = System.currentTimeMillis()
                val langCode = java.util.Locale.getDefault().language
                val response = api.getGames(langCode)

                if (response.isSuccessful) {
                    val apiGames = response.body()?.toDomain() ?: emptyList()
                    gamesStateFlow.value = apiGames
                    val result = Result.success(apiGames)

                    // Calculate elapsed time and add delay if needed
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_REQUEST_DURATION) {
                        delay(MIN_REQUEST_DURATION - elapsedTime)
                    }

                    emit(result)
                } else {
                    val firebaseInstance = FirebaseCrashlytics.getInstance()
                    firebaseInstance.setCustomKey("loaded_data", "games")
                    firebaseInstance.setCustomKey("fun name", "getGames")
                    firebaseInstance.setCustomKey("lang code", langCode)
                    val errorBody = response.errorBody()?.string() ?: "Unknown error - getGames"
                    val exception =
                        Exception("API call failed with code ${response.code()}: $errorBody")
                    emit(Result.failure(exception))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Refresh the games data from the remote source.
     */
    override suspend fun refreshGames() {
        val langCode = java.util.Locale.getDefault().language
        val response = api.getGames(langCode)
        if (response.isSuccessful) {
            val apiGames = response.body()?.toDomain() ?: emptyList()
            gamesStateFlow.value = apiGames
        }
    }

    /**
     * Observe the cached games data.
     *
     * @return A StateFlow emitting the cached list of Games.
     */
    override fun observeCachedGames(): StateFlow<List<Game>?> = gamesStateFlow

    /**
     * Get a specific game by its ID.
     *
     * @param id The ID of the game to retrieve.
     * @return A Flow emitting a Result containing the requested Game.
     */
    override fun getGame(id: Int): Flow<Result<Game>> = flow {
        FirebaseCrashlytics.getInstance().setCustomKey("game_id", id)
        try {
            // Check if the game is in the cache
            gamesStateFlow.value?.find { it.id == id }?.let {
                emit(Result.success(it))
                return@flow
            }

            // If not in cache or we need to reload, fetch from API
            val startTime = System.currentTimeMillis()
            val langCode = java.util.Locale.getDefault().language
            val response = api.getGame(storyId = id, langCode = langCode)

            if (response.isSuccessful) {
                val game = response.body()?.toDomain()
                if (game != null) {
                    // Update the cache with the new game
                    gamesStateFlow.value?.let { currentGames ->
                        val updatedGames = currentGames.toMutableList()
                        val existingIndex = updatedGames.indexOfFirst { it.id == game.id }
                        if (existingIndex >= 0) {
                            updatedGames[existingIndex] = game
                        } else {
                            updatedGames.add(game)
                        }
                        gamesStateFlow.value = updatedGames
                    }

                    val result = Result.success(game)

                    // Calculate elapsed time and add delay if needed
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_REQUEST_DURATION) {
                        delay(MIN_REQUEST_DURATION - elapsedTime)
                    }

                    emit(result)
                } else {
                    emit(Result.failure(Exception("Game not found")))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error - getGame"
                val exception =
                    Exception("API call failed with code ${response.code()}: $errorBody")
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Check if a game is updatable by comparing the local version with the remote version.
     *
     * @param game The game to check for updates.
     * @return True if the game is updatable, false otherwise.
     */
    override suspend fun isGameUpdatable(game: Game): Flow<Result<Boolean>> = flow {
        val currentVersion = tableOfSymbols.getStoryVersion(game.id)
        val result = currentVersion != game.versionCode && currentVersion != "none"
                && currentVersion.isNotBlank()
        emit(
            Result.success(result)
        )
    }

    override suspend fun hasGameLocalFiles(game: Game): Flow<Result<Boolean>> = flow {
        try {
            val res = tableOfSymbols.getStoryVersion(game.id) == game.versionCode
            emit(Result.success(res))
        } catch (e: Exception) {
            emit(
                Result.failure(
                    Exception("An error occurred while checking for local files: ${e.message}")
                )
            )
        }
    }

    override suspend fun setGameVersion(game: Game): Flow<Result<Unit>> = flow {
        tableOfSymbols.setStoryVersion(rowId = game.id, version = game.versionCode)
        tableOfSymbols.save(context = context)
        emit(Result.success(Unit))
    }

    override suspend fun removeGame(game: Game): Flow<Result<Unit>> = flow {
        tableOfSymbols.deleteRowData(rowId = game.id)
        tableOfSymbols.save(context = context)
        emit(Result.success(Unit))
    }

    override fun isFriendzonedGame(game: Game): Boolean {
        return listOf(159, 161, 162, 163).contains(game.id)
    }

    override fun isFriendzoned1Game(game: Game): Boolean {
        return 162 == game.id
    }

    /**
     * Generate a download link for a game
     *
     * @param userId The ID of the user requesting the download
     * @param userToken The token of the user requesting the download
     * @param gameId The ID of the game to download
     * @throws GameDownloadForbiddenException if the game download is forbidden
     * @return A Flow containing the Result of the operation with the download link as a String
     */
    override suspend fun generateGameDownloadLink(
        userId: String,
        userToken: String,
        gameId: Int
    ): Flow<Result<String>> = flow {
        FirebaseCrashlytics.getInstance().setCustomKey("user_id", userId)
        FirebaseCrashlytics.getInstance().setCustomKey("downloading_game", gameId)
        try {
            val body = DownloadLinkRequestDto(userId, userToken, gameId)
            val response = api.generateGameDownloadLink(body = body)
            if (response.isSuccessful) {
                val downloadLinkResponse = response.body()
                val downloadLink = downloadLinkResponse?.link ?: ""
                emit(Result.success(downloadLink))
            } else {
                val errorBody =
                    response.errorBody()?.string() ?: "Unknown error - generateGameDownloadLink"
                val exception = if (response.code() == 403) {
                    GameDownloadForbiddenException("Game download forbidden (403): $errorBody")
                } else {
                    Exception("API call failed with code ${response.code()}: $errorBody")
                }
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun generateFreeGameDownloadLink(gameId: Int): Flow<Result<String>> = flow {
        FirebaseCrashlytics.getInstance().setCustomKey("generating_download_link_game_id", gameId)
        FirebaseCrashlytics.getInstance().setCustomKey("generating_download_link_game_type", "free")
        try {
            val body = FreeDownloadLinkRequestDto(gameId)
            val response = api.generateFreeGameDownloadLink(body = body)
            if (response.isSuccessful) {
                val downloadLinkResponse = response.body()
                val downloadLink = downloadLinkResponse?.link ?: ""
                emit(Result.success(downloadLink))
            } else {
                val errorBody =
                    response.errorBody()?.string() ?: "Unknown error - generateFreeGameDownloadLink"
                val exception = if (response.code() == 403) {
                    GameDownloadForbiddenException("Game download forbidden (403): $errorBody")
                } else {
                    Exception("API call failed with code ${response.code()}: $errorBody")
                }
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
