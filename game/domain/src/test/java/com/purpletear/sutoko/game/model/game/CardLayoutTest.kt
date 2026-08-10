package com.purpletear.sutoko.game.model.game

import org.junit.Assert.assertEquals
import org.junit.Test

class CardLayoutTest {

    @Test
    fun `known values map to their enum entry`() {
        assertEquals(CardLayout.HORIZONTAL, CardLayout.fromRaw("HORIZONTAL"))
        assertEquals(CardLayout.VERTICAL, CardLayout.fromRaw("VERTICAL"))
    }

    @Test
    fun `server values are case-insensitive`() {
        assertEquals(CardLayout.HORIZONTAL, CardLayout.fromRaw("horizontal"))
        assertEquals(CardLayout.VERTICAL, CardLayout.fromRaw("vertical"))
    }

    @Test
    fun `missing or unknown values fall back to HORIZONTAL`() {
        assertEquals(CardLayout.HORIZONTAL, CardLayout.fromRaw(null))
        assertEquals(CardLayout.HORIZONTAL, CardLayout.fromRaw(""))
        assertEquals(CardLayout.HORIZONTAL, CardLayout.fromRaw("DIAGONAL"))
    }
}
