package com.purpletear.game.presentation.game_play.chapter

import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Owns the transition offered at the end of a chapter. Availability is fail-closed:
 * a missing chapter or a failed lookup is treated as unavailable because navigating
 * there would be a dead end anyway.
 */
class NextChapterController(
    private val gameId: String,
    private val chapterRepository: ChapterRepository,
    private val logger: Logger,
    private val scope: CoroutineScope,
    private val updateState: (transform: (GameUiState) -> GameUiState) -> Unit,
) {
    private val _navigateToNextChapter = Channel<String>(Channel.BUFFERED)
    val navigateToNextChapter: Flow<String> = _navigateToNextChapter.receiveAsFlow()

    private var pendingChapterCode: String? = null

    fun onChapterChange(nextChapterCode: String) {
        pendingChapterCode = nextChapterCode
        updateState { it.copy(isNextChapterAvailabilityResolved = false) }
        checkAvailability(nextChapterCode)
    }

    fun onNextChapterClicked(isAvailable: Boolean) {
        if (!isAvailable) return
        pendingChapterCode?.let { _navigateToNextChapter.trySend(it) }
    }

    fun reset() {
        pendingChapterCode = null
    }

    private fun checkAvailability(nextChapterCode: String) {
        scope.launch {
            val next = chapterRepository.observeChapters(gameId)
                .catch { error ->
                    logger.exception(error) { "next chapter availability check failed" }
                    emit(emptyList())
                }
                .first()
                .firstOrNull { it.normalizedCode == nextChapterCode.lowercase() }
            updateState {
                it.copy(
                    isNextChapterAvailable = next?.available == true,
                    isNextChapterAvailabilityResolved = true,
                    nextChapterReleaseDate = next?.releaseDate?.takeIf { date -> date > 0 },
                )
            }
        }
    }
}
