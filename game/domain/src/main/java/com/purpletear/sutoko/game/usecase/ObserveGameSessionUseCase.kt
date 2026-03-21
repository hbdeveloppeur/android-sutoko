package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.ErrorType
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes game session state reactively.
 * Emits updated states when user progress changes (e.g., chapter advancement).
 *
 * Flow: progress.chapterCode changes → load chapters → find matching chapter → emit Ready state
 */
class ObserveGameSessionUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val getChapters: GetChaptersUseCase
) {
    operator fun invoke(gameId: String): Flow<GameSessionState> {
        return userGameProgressRepository.observe(gameId)
            .map { progress -> progress.normalizedChapterCode }
            .distinctUntilChanged()
            .flatMapLatest { chapterCode ->
                loadSessionForChapter(gameId, chapterCode)
            }
    }

    private fun loadSessionForChapter(gameId: String, chapterCode: String): Flow<GameSessionState> = flow {
        emit(GameSessionState.Loading)

        val result = try {
            getChapters(gameId).firstOrNull()
        } catch (e: Exception) {
            null
        }

        val chapters = result?.getOrNull()

        if (chapters.isNullOrEmpty()) {
            emit(
                GameSessionState.Error(
                    type = ErrorType.NO_CHAPTERS_FOUND,
                    message = "No chapters available"
                )
            )
            return@flow
        }

        val targetChapter = chapters.find { it.normalizedCode == chapterCode }
            ?: chapters.firstOrNull()

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

        val progress = userGameProgressRepository.get(gameId)

        emit(
            GameSessionState.Ready(
                gameId = gameId,
                chapter = targetChapter,
                heroName = progress?.heroName ?: "",
                totalChapters = chapters.size
            )
        )
    }
}
