package com.purpletear.game.data.download

import android.util.Log
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.ExtractZipParams
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.repository.GameRepository
import com.purpletear.sutoko.game.repository.ZipRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of [GameDownloadManager] that handles the complete download workflow:
 * 1. Generate download link (authenticated or free)
 * 2. Download the game archive using PRDownloader
 * 3. Extract the zip file
 * 4. Set the game version
 *
 * This implementation maintains download state per gameId and supports cancellation.
 */
@Singleton
class GameDownloadManagerImpl @Inject constructor(
    private val gameRepository: GameRepository,
    private val zipRepository: ZipRepository,
    private val gamePathProvider: GamePathProvider,
    private val ntfy: Ntfy,
) : GameDownloadManager {

    private val tag = "GameDownloadManager"

    /**
     * Map to store download state flows per gameId.
     * Using ConcurrentHashMap for thread-safe access.
     */
    private val downloadStates = ConcurrentHashMap<String, MutableStateFlow<GameDownloadState>>()

    /**
     * Map to store coroutine scopes per gameId for proper lifecycle management.
     */
    private val downloadScopes = ConcurrentHashMap<String, CoroutineScope>()

    override fun getDownloadState(gameId: String): StateFlow<GameDownloadState> {
        return downloadStates.getOrPut(gameId) {
            MutableStateFlow(GameDownloadState.Idle)
        }
    }

    override suspend fun downloadGame(
        game: Game,
        userId: String?,
        userToken: String?
    ) {
        // Check if already downloading
        val currentState = getDownloadState(game.id).value
        if (currentState is GameDownloadState.Downloading ||
            currentState is GameDownloadState.Extracting
        ) {
            Log.d(tag, "Download already in progress for game ${game.id}")
            return
        }

        // Validate credentials for premium games
        if (game.isPremium() && (userId.isNullOrBlank() || userToken.isNullOrBlank())) {
            throw GameDownloadForbiddenException("User authentication required for premium game download")
        }

        // Create a new scope for this download
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        downloadScopes[game.id] = scope

        val stateFlow = downloadStates.getOrPut(game.id) {
            MutableStateFlow(GameDownloadState.Idle)
        }

        scope.launch {
            try {
                // Check if already cancelled before starting
                if (!isActive) return@launch
                
                // Reset state to Downloading at the start
                stateFlow.value = GameDownloadState.Downloading(0)

                // Step 1: Generate download link
                val downloadLink = generateDownloadLink(game.id, game.isPremium(), userId, userToken)

                // Step 2: Download the archive
                val destinationPath = gamePathProvider.getStoryDirectoryPath(game.id)
                val fileName = "game_${game.id}.zip"

                downloadArchive(game.id, downloadLink, destinationPath, fileName, stateFlow)

            } catch (e: CancellationException) {
                // Normal cancellation - don't change state (cancelDownload already set it)
                Log.d(tag, "Download cancelled for game ${game.id}")
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Download failed for game ${game.id}", e)
                // Only set error if not already cancelled
                if (stateFlow.value !is GameDownloadState.Cancelled) {
                    stateFlow.value = GameDownloadState.Error(e)
                }
            } finally {
                // Clean up scope after download completes (success, error, or cancellation)
                downloadScopes.remove(game.id)
            }
        }
    }

    override fun cancelDownload(gameId: String) {
        PRDownloader.cancel(gameId)
        downloadScopes[gameId]?.cancel()
        downloadScopes.remove(gameId)

        val stateFlow = downloadStates[gameId]
        val currentState = stateFlow?.value
        if (currentState is GameDownloadState.Downloading ||
            currentState is GameDownloadState.Extracting
        ) {
            stateFlow.value = GameDownloadState.Cancelled
        }
    }

    override fun cleanup(gameId: String) {
        cancelDownload(gameId)
        downloadStates.remove(gameId)
    }

    /**
     * Generates a download link for the game based on whether it's premium or free.
     *
     * @param gameId The game identifier
     * @param isPremium Whether the game requires authentication
     * @param userId The user ID for authenticated downloads
     * @param userToken The user token for authenticated downloads
     * @return The generated download link
     * @throws GameDownloadForbiddenException if the user doesn't have permission
     */
    private suspend fun generateDownloadLink(
        gameId: String,
        isPremium: Boolean,
        userId: String?,
        userToken: String?
    ): String {
        return if (isPremium) {
            val uid = userId ?: throw GameDownloadForbiddenException("userId is required for premium game downloads")
            val token = userToken ?: throw GameDownloadForbiddenException("userToken is required for premium game downloads")
            gameRepository.generateGameDownloadLink(
                userId = uid,
                userToken = token,
                gameId = gameId
            ).first().getOrThrow()
        } else {
            // Free games don't require authentication
            gameRepository.generateFreeGameDownloadLink(gameId = gameId).first().getOrThrow()
        }
    }

    /**
     * Downloads the game archive using PRDownloader.
     * This is a suspending function that completes when download finishes or fails.
     *
     * @param gameId The game identifier (used as PRDownloader tag)
     * @param link The download URL
     * @param destinationPath Where to save the file
     * @param fileName The name of the zip file
     * @param stateFlow The state flow to emit progress updates
     */
    private suspend fun downloadArchive(
        gameId: String,
        link: String,
        destinationPath: String,
        fileName: String,
        stateFlow: MutableStateFlow<GameDownloadState>,
    ) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val minDurationMs = 4000L // 4 seconds minimum
        
        ntfy.startAction("Downloading archive for game $gameId")
        // Cancel any existing download with same tag
        PRDownloader.cancel(gameId)

        suspendCancellableCoroutine { continuation: CancellableContinuation<Unit> ->
            val downloadId = PRDownloader.download(link, destinationPath, fileName)
                .setTag(gameId)
                .build()
                .setOnProgressListener { progress ->
                    progress?.let {
                        val downloadProgress = if (it.totalBytes > 0) {
                            (it.currentBytes * 100 / it.totalBytes).toInt()
                        } else 0
                        stateFlow.value = GameDownloadState.Downloading(downloadProgress)
                    }
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(error: Error?) {
                        if (continuation.isActive) {
                            val errorMessage = error?.connectionException?.message
                                ?: error?.serverErrorMessage
                                ?: "Unknown download error"
                            Log.e(tag, "Download error for game $gameId: $errorMessage")
                            val exception = RuntimeException(errorMessage)
                            stateFlow.value = GameDownloadState.Error(exception)
                            continuation.resumeWithException(exception)
                        }
                    }
                })

            // Handle cancellation
            continuation.invokeOnCancellation {
                PRDownloader.cancel(downloadId)
            }
        }

        // After download completes (successfully or with error), handle extraction if successful
        if (stateFlow.value is GameDownloadState.Downloading ||
            (stateFlow.value as? GameDownloadState.Downloading)?.progress == 100) {
            try {
                ntfy.startAction("Extracting archive for game $gameId to destination: $destinationPath")
                stateFlow.value = GameDownloadState.Extracting
                val zipFile = File(destinationPath, fileName)
                extractZipFile(zipFile, destinationPath, gameId)
                
                // Ensure minimum duration of 4 seconds for better UX
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = minDurationMs - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }
                
                stateFlow.value = GameDownloadState.Completed
            } catch (e: CancellationException) {
                Log.d(tag, "Extraction cancelled for game $gameId")
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Extraction failed for game $gameId", e)
                ntfy.urgent(e)
                if (stateFlow.value !is GameDownloadState.Cancelled) {
                    stateFlow.value = GameDownloadState.Error(e)
                }
            }
        }
    }

    /**
     * Extracts the zip file and sets the game version.
     *
     * @param zipFile The zip file to extract
     * @param destinationPath Where to extract the contents
     * @param gameId The game identifier for setting version
     * @throws Exception if extraction fails
     */
    private suspend fun extractZipFile(
        zipFile: File,
        destinationPath: String,
        gameId: String
    ) {
        val params = ExtractZipParams(
            zipFile = zipFile,
            destinationPath = destinationPath,
            deleteArchiveAfterExtraction = true,
        )

        zipRepository.extractZip(params).first().getOrThrow()

        // Set game version after successful extraction
        // Note: We need the Game object for this. Since we don't have it here,
        // the ViewModel will handle this step after receiving Completed state.
    }

    override suspend fun setGameVersion(game: Game) {
        gameRepository.setGameVersion(game).first().getOrThrow()
    }

    override fun resetState(gameId: String) {
        // Cancel any ongoing download first
        cancelDownload(gameId)
        // Remove the state flow from memory
        downloadStates.remove(gameId)
    }
}
