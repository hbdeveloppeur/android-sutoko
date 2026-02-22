package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving chapters for a specific story.
 */
class GetChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to get chapters for a specific story.
     *
     * @param storyId The ID of the story to retrieve chapters for.
     * @return A Flow emitting a Result containing the list of Chapters.
     */
    operator fun invoke(storyId: String): Flow<Result<List<Chapter>>> {
        return chapterRepository.getChapters(storyId)
    }
}
