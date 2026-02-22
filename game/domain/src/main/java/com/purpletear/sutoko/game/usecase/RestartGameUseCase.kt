package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case for restarting a game by resetting its current chapter and related progress.
 */
class RestartGameUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to restart a specific game.
     *
     * @param gameId The ID of the game to restart.
     */
    suspend operator fun invoke(gameId: String) {
        chapterRepository.restart(gameId)
    }
}
