package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.game.GameRepository
import javax.inject.Inject

/**
 * Use case for loading the next page of user-created games.
 */
class LoadMoreUserGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Load the next page of user-created games.
     *
     * @param languageTag The BCP-47 language tag to request games for.
     * @return A Result containing `true` if more pages remain, `false` otherwise.
     */
    suspend operator fun invoke(languageTag: String): Result<Boolean> {
        return gameRepository.loadMoreUserGames(languageTag)
    }
}
