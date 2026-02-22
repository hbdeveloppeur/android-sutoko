package com.purpletear.sutoko.game.usecase

import javax.inject.Inject


class IsFriendZonedGameUseCase @Inject constructor(
) {

    operator fun invoke(gameId: String): Boolean {
        // Legacy game IDs - now using hashCode for compatibility with string-based IDs
        return gameId.hashCode() in intArrayOf(159, 161, 162, 163)
    }
}
