package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for observing the download state of a game.
 * Returns a StateFlow that emits download progress updates.
 */
class ObserveDownloadStateUseCase @Inject constructor(
    private val gameDownloadManager: GameDownloadManager,
) {
    /**
     * Invoke the use case to observe download state for a specific game.
     *
     * @param gameId The unique identifier of the game
     * @return StateFlow of the current download state
     */
    operator fun invoke(gameId: String): StateFlow<GameDownloadState> {
        return gameDownloadManager.getDownloadState(gameId)
    }
}
