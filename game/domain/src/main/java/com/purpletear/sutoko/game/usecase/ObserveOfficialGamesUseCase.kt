package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOfficialGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<List<GameCatalog>> {
        return gameRepository.observeOfficialGames()
    }
}
