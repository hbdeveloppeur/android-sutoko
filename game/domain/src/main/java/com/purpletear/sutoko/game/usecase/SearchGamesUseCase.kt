package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching stories by query string.
 * Searches story titles, author names, and categories.
 */
class SearchStoriesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to search for stories.
     *
     * @param query The search query (2-100 characters)
     * @param languageCode The language code (e.g., "fr-FR", "en-US")
     * @param page The page number (starting from 1, default: 1)
     * @param limit The number of items per page (1-20, default: 20)
     * @return A Flow emitting a Result containing a list of Games matching the search criteria
     */
    operator fun invoke(
        query: String,
        languageCode: String = "fr-FR",
        page: Int = 1,
        limit: Int = 20,
    ): Flow<Result<List<Game>>> {
        return gameRepository.searchStories(
            query = query,
            languageCode = languageCode,
            page = page,
            limit = limit
        )
    }
}
