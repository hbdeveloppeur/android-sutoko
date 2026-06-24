package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import javax.inject.Inject

class SearchGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        query: String,
        languageTag: String,
        page: Int = 1,
        limit: Int = 20,
    ): Result<List<GameCatalog>> = gameRepository.searchStories(query, languageTag, page, limit)
}
