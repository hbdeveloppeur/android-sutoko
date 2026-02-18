package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import javax.inject.Inject


class IsFriendZoned1GameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    operator fun invoke(game: Game): Boolean {
        return gameRepository.isFriendzoned1Game(game)
    }
}