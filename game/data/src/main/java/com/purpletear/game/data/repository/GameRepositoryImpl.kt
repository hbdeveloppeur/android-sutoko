package com.purpletear.game.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.GamePortalApi
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.game.data.utils.ifNotCancellation
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameInstallationRepository
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Implementation of the GameRepository interface.
 */
class GameRepositoryImpl @Inject constructor(
    private val api: GameApi,
    private val portalApi: GamePortalApi,
    private val gameInstallationRepository: GameInstallationRepository,
    private val ntfy: Ntfy,
) : GameRepository {

    // Thread-safe and observable cache
    private val officialGamesStateFlow = MutableStateFlow<List<Game>?>(null)
    private val usersGamesStateFlow = MutableStateFlow<List<Game>?>(null)

    // Installation status cache - single source of truth for game installation state
    private val installationStatusFlows = ConcurrentHashMap<String, MutableStateFlow<Boolean>>()

    /**
     * Get or create a StateFlow for a game's installation status.
     * Initial value is determined by checking local files.
     */
    private fun getOrCreateInstallationStatusFlow(gameId: String): MutableStateFlow<Boolean> {
        return installationStatusFlows.getOrPut(gameId) {
            val initialValue = checkGameInstalled(gameId)
            MutableStateFlow(initialValue)
        }
    }

    /**
     * Check if a game is installed by reading from GameInstallationRepository.
     * Uses runBlocking for initial synchronous check in getOrCreateInstallationStatusFlow.
     */
    private fun checkGameInstalled(gameId: String): Boolean = runBlocking {
        gameInstallationRepository.isInstalled(gameId)
    }

    /**
     * Get a list of official games.
     *
     * @return A Flow emitting a Result containing a list of Games.
     */
    override fun getOfficialGames(): Flow<Result<List<Game>>> = flow {
        // TODO: Implement when official games endpoint is ready
        emit(Result.success(emptyList()))
    }

    /**
     * Get a paginated list of user-created games.
     *
     * @param languageCode The language code (e.g., "fr-FR")
     * @param page The page number (starting from 1)
     * @param limit The number of items per page
     * @return A Flow emitting a Result containing a list of Games.
     */
    override fun getUsersGames(
        languageCode: String,
        page: Int,
        limit: Int,
    ): Flow<Result<List<Game>>> = flow {
        try {
            val response = api.getUserGames(
                languageCode = languageCode,
                page = page,
                limit = limit
            )

            if (response.isSuccessful) {
                val games = response.body()?.toDomain() ?: emptyList()
                usersGamesStateFlow.value = games
                emit(Result.success(games))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                emit(Result.failure(Exception("Failed to load user games: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            e.ifNotCancellation { FirebaseCrashlytics.getInstance().recordException(it) }
            emit(Result.failure(e))
        }
    }

    /**
     * Observe the cached games data.
     *
     * @return A StateFlow emitting the cached list of Games.
     */
    override fun observeCachedOfficialGames(): StateFlow<List<Game>?> = officialGamesStateFlow
    override fun observeCachedUsersGames(): StateFlow<List<Game>?> = usersGamesStateFlow

    /**
     * Get a specific game by its ID.
     * First checks cache, then fetches from API.
     *
     * @param id The ID of the game to retrieve.
     * @return A Flow emitting a Result containing the requested Game.
     */
    override fun getGame(id: String): Flow<Result<Game>> = flow {
        // Check cache first (from both official and user games)
        val cachedGame = officialGamesStateFlow.value?.find { it.id == id }
            ?: usersGamesStateFlow.value?.find { it.id == id }

        cachedGame?.let {
            emit(Result.success(it))
        }

        // Fetch from API
        try {
            // TODO
            val response = api.getGame(storyId = id, langCode = "fr-FR")

            if (response.isSuccessful) {
                val game = response.body()?.toDomain()
                if (game != null) {
                    emit(Result.success(game))
                } else {
                    if (cachedGame == null) {
                        emit(Result.failure(Exception("Game not found")))
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                if (cachedGame == null) {
                    emit(Result.failure(Exception("Failed to load game: ${response.code()} - $errorBody")))
                }
            }
        } catch (e: Exception) {
            e.ifNotCancellation { FirebaseCrashlytics.getInstance().recordException(it) }
            if (cachedGame == null) {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Check if a game is updatable by comparing the local version with the remote version.
     *
     * @param game The game to check for updates.
     * @return True if the game is updatable, false otherwise.
     */
    override suspend fun isGameUpdatable(game: Game): Flow<Result<Boolean>> = flow {
        val currentVersion = gameInstallationRepository.getInstalledVersion(game.id)
        val result = currentVersion != null
                && currentVersion != game.version.toString()
                && currentVersion != "none"
                && currentVersion.isNotBlank()
        emit(Result.success(result))
    }

    override suspend fun hasGameLocalFiles(game: Game): Flow<Result<Boolean>> = flow {
        try {
            val currentVersion = gameInstallationRepository.getInstalledVersion(game.id)
            val res = currentVersion == game.version.toString()
            emit(Result.success(res))
        } catch (e: Exception) {
            emit(
                Result.failure(
                    Exception("An error occurred while checking for local files: ${e.message}")
                )
            )
        }
    }

    override fun observeGameInstallationStatus(gameId: String): StateFlow<Boolean> {
        return getOrCreateInstallationStatusFlow(gameId)
    }

    override suspend fun removeGame(gameId: String): Flow<Result<Unit>> = flow {
        gameInstallationRepository.removeInstallation(gameId)

        installationStatusFlows[gameId]?.value = false

        emit(Result.success(Unit))
    }

    override fun isFriendzonedGame(game: Game): Boolean {
        // Legacy check based on old Int IDs - now using hashCode for compatibility
        return listOf(159, 161, 162, 163).contains(game.id.hashCode())
    }

    override fun isFriendzoned1Game(game: Game): Boolean {
        // Legacy check based on old Int ID 162 - now using hashCode for compatibility  
        return 162 == game.id.hashCode()
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
        gameId: String
    ): Flow<Result<String>> {
        FirebaseCrashlytics.getInstance().setCustomKey("user_id", userId)
        FirebaseCrashlytics.getInstance().setCustomKey("downloading_game", gameId)
        return generateDownloadLinkInternal(
            gameId = gameId,
            userId = userId,
            userToken = userToken,
            errorContext = "generateGameDownloadLink",
        )
    }

    override suspend fun generateFreeGameDownloadLink(gameId: String): Flow<Result<String>> {
        FirebaseCrashlytics.getInstance().setCustomKey("generating_download_link_game_id", gameId)
        FirebaseCrashlytics.getInstance().setCustomKey("generating_download_link_game_type", "free")
        return generateDownloadLinkInternal(
            gameId = gameId,
            userId = null,
            userToken = null,
            errorContext = "generateFreeGameDownloadLink",
        )
    }

    private fun generateDownloadLinkInternal(
        gameId: String,
        userId: String?,
        userToken: String?,
        errorContext: String,
    ): Flow<Result<String>> = flow {
        ntfy.startAction("Generating download link for game $gameId")
        try {
            val response = api.generateGameDownloadLink(
                gameId = gameId,
                userId = userId,
                userToken = userToken,
            )
            if (response.isSuccessful) {
                val downloadLink = response.body()?.link
                if (downloadLink.isNullOrBlank()) {
                    val exception =
                        IllegalStateException("Empty download link for gameId: $gameId, userId: $userId")
                    ntfy.urgent(exception)
                    emit(Result.failure(exception))
                    return@flow
                }
                emit(Result.success(downloadLink))
            } else {
                val url = response.raw().request.url.toString()
                ntfy.startAction("Generating download link for game $gameId, at url : $url")

                val errorBody = response.errorBody()?.string()
                    ?: "Unknown error - $errorContext"
                val exception = if (response.code() == 403) {
                    GameDownloadForbiddenException("Game download forbidden (403): $errorBody")
                } else {
                    Exception("API call failed with code ${response.code()}: $errorBody")
                }
                ntfy.exception(exception)
                emit(Result.failure(exception))
            }
        } catch (_: CancellationException) {
            // Silent. Excepted.
        } catch (e: Exception) {
            ntfy.exception(e)
            emit(Result.failure(e))
        }
    }

    /**
     * Search for stories by query string.
     * Searches story titles, author names, and categories.
     *
     * @param query The search query (2-100 characters)
     * @param languageCode The language code (e.g., "fr-FR", "en-US")
     * @param page The page number (starting from 1, default: 1)
     * @param limit The number of items per page (1-20, default: 20)
     * @return A Flow emitting a Result containing a list of Games matching the search criteria
     */
    override fun searchStories(
        query: String,
        languageCode: String,
        page: Int,
        limit: Int,
    ): Flow<Result<List<Game>>> = flow {
        try {
            val response = portalApi.searchStories(
                query = query,
                languageCode = languageCode,
                page = page,
                limit = limit
            )

            if (response.isSuccessful) {
                val games = response.body()?.toDomain() ?: emptyList()
                emit(Result.success(games))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                emit(Result.failure(Exception("Search failed: ${response.code()} - $errorBody")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.ifNotCancellation { FirebaseCrashlytics.getInstance().recordException(it) }
            emit(Result.failure(e))
        }
    }
}
