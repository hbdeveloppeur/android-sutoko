package fr.purpletear.sutoko.screens.account.screen.transformers

import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.screens.account.screen.model.GameWithOwnership

object PossessedGamesTransformer {

    /**
     * Transforms a list of [Game] objects and a set of possessed game IDs into a list of [GameWithOwnership] objects.
     * Sorted by possession status.
     * @param games A list of [Game] objects representing games.
     * @param possessedGameIds A set of Integers representing the IDs of possessed games.
     * @return A list of [GameWithOwnership] objects created by mapping each game in the `games` list to a new `GameWithOwnership` object,
     *         with the ownership status determined by the presence of the game's ID in the `possessedGameIds` set.
     */
    fun transform(games: List<Game>, possessedGameIds: Set<Int>): List<GameWithOwnership> {
        return games.map { game ->
            GameWithOwnership(
                game,
                possessedGameIds.contains(game.id)
            )
        }.sortedByDescending { it.isPossessed }
    }
}
