package fr.purpletear.sutoko.screens.main.presentation

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import com.purpletear.sutoko.game.model.game.CardLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeCatalogStateTest {
    @Test
    fun `four stories remain visible without a banner section`() {
        val stories = (1..4).map { story(it) }
        val catalog = HomeCatalogState.fromGames(stories)
        assertEquals(stories, catalog.squareStories)
        assertTrue(catalog.fullStories.isEmpty())
        assertTrue(catalog.hasStories)
    }

    @Test
    fun `initial state is distinct from an empty cache`() {
        assertFalse(HomeCatalogState().isCacheLoaded)
        val empty = HomeCatalogState.fromGames(emptyList())
        assertTrue(empty.isCacheLoaded)
        assertFalse(empty.hasStories)
    }

    @Test
    fun `small catalogs render as banners without dropping stories`() {
        for (count in 1..3) {
            val stories = (1..count).map { story(it) }
            val catalog = HomeCatalogState.fromGames(stories)
            assertEquals(stories, catalog.fullStories)
            assertTrue(catalog.squareStories.isEmpty())
            assertTrue(catalog.hasStories)
        }
    }

    @Test
    fun `mixed layouts keep backend order within each section`() {
        val ordered = listOf(story(8), story(2, CardLayout.VERTICAL), story(3), story(1), story(9), story(4), story(5, CardLayout.VERTICAL))
        val catalog = HomeCatalogState.fromGames(ordered)
        assertEquals(listOf("8", "3", "1", "9"), catalog.squareStories.map { it.id })
        assertEquals(listOf("4"), catalog.fullStories.map { it.id })
        assertEquals(listOf("2", "5"), catalog.verticalStories.map { it.id })
    }

    @Test
    fun `portrait only catalogs remain visible`() {
        val stories = listOf(story(1, CardLayout.VERTICAL), story(2, CardLayout.VERTICAL))
        val catalog = HomeCatalogState.fromGames(stories)
        assertEquals(stories, catalog.verticalStories)
        assertTrue(catalog.squareStories.isEmpty())
        assertTrue(catalog.fullStories.isEmpty())
        assertTrue(catalog.hasStories)
    }

    private fun story(id: Int, layout: CardLayout = CardLayout.HORIZONTAL) = GameCatalog(
        id = "$id",
        metadata = GameMetadata(title = "Story $id"),
        cardLayout = layout,
        canvasTechnologyRequiredVersion = 1,
    )
}
