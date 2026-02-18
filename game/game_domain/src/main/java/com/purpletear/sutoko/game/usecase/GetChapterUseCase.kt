package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a specific chapter by ID.
 */
class GetChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to get a specific chapter by ID.
     *
     * @param id The ID of the chapter to retrieve.
     * @return A Flow emitting a Result containing the requested Chapter.
     */
    operator fun invoke(id: Int): Flow<Result<Chapter>> {
        return chapterRepository.getChapter(id)
    }
}
