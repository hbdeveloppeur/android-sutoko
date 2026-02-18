package com.purpletear.sutoko.game.usecase

import javax.inject.Inject


class IsFriendZonedGameUseCase @Inject constructor(
) {

    operator fun invoke(gameId: Int): Boolean {
        return gameId in intArrayOf(159, 161, 162, 163)
    }
}