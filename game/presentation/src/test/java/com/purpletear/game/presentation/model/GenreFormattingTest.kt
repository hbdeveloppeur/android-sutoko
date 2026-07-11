package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.game.NarrativeTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class GenreFormattingTest {

    @Test
    fun `empty themes fall back to the provided fallback`() {
        assertEquals(FALLBACK, formatNarrativeThemes(emptyList(), FALLBACK))
    }

    @Test
    fun `single theme displays its localized name without a separator`() {
        assertEquals(
            "Drame",
            formatNarrativeThemes(listOf(NarrativeTheme(id = "drama", name = "Drame")), FALLBACK),
        )
    }

    @Test
    fun `multiple themes join their localized names with the bullet separator`() {
        val themes = listOf(
            NarrativeTheme(id = "drama", name = "Drame"),
            NarrativeTheme(id = "funny", name = "Dr\u00f4le"),
            NarrativeTheme(id = "romance", name = "Romance"),
        )
        assertEquals("Drame • Dr\u00f4le • Romance", formatNarrativeThemes(themes, FALLBACK))
    }

    private companion object {
        const val FALLBACK = "Police • Romance • Drama"
    }
}
