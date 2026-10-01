package fr.purpletear.sutoko.screens.main.presentation

import com.purpletear.sutoko.game.model.game.CardLayout
import com.purpletear.sutoko.game.model.game.GameCatalog

/** One catalog snapshot, so sections never render from different repository emissions. */
data class HomeCatalogState(
    val squareStories: List<GameCatalog> = emptyList(),
    val fullStories: List<GameCatalog> = emptyList(),
    val verticalStories: List<GameCatalog> = emptyList(),
    val isCacheLoaded: Boolean = false,
) {
    val hasStories: Boolean
        get() = squareStories.isNotEmpty() || fullStories.isNotEmpty() || verticalStories.isNotEmpty()

    companion object {
        fun fromGames(orderedGames: List<GameCatalog>): HomeCatalogState {
            val (vertical, horizontal) = orderedGames.partition { it.cardLayout == CardLayout.VERTICAL }
            val hasSquareRow = horizontal.size >= 4
            return HomeCatalogState(
                squareStories = if (hasSquareRow) horizontal.take(4) else emptyList(),
                fullStories = if (hasSquareRow) horizontal.drop(4) else horizontal,
                verticalStories = vertical,
                isCacheLoaded = true,
            )
        }
    }
}
