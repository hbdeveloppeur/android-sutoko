package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.ErrorType
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Starts a game session by validating game availability and loading the current chapter.
 * Emits states: Loading → Ready | Error
 */
class StartGameSessionUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val userGameProgressRepository: UserGameProgressRepository
) {
    operator fun invoke(gameId: String, isGranted: Boolean): Flow<GameSessionState> = flow {
        emit(GameSessionState.Loading)

        val chapters = chapterRepository.getChapters(gameId).firstOrNull()?.getOrNull()

        if (chapters.isNullOrEmpty()) {
            emit(
                GameSessionState.Error(
                    type = ErrorType.NO_CHAPTERS_FOUND,
                    message = "No chapters available"
                )
            )
            return@flow
        }

        val progress = userGameProgressRepository.get(gameId)
        val targetChapter = progress?.let { p ->
            chapters.find { it.number == p.currentChapterNumber && it.alternative == p.currentAlternative }
        } ?: chapters.firstOrNull()

        if (targetChapter == null) {
            emit(
                GameSessionState.Error(
                    type = ErrorType.CHAPTER_NOT_FOUND,
                    message = "No available chapter found"
                )
            )
            return@flow
        }

        if (!targetChapter.isAvailable) {
            emit(
                GameSessionState.Error(
                    type = ErrorType.CHAPTER_UNAVAILABLE,
                    message = "Chapter not yet released"
                )
            )
            return@flow
        }

        emit(
            GameSessionState.Ready(
                gameId = gameId,
                chapter = targetChapter,
                heroName = progress?.heroName ?: ""
            )
        )
    }
}
