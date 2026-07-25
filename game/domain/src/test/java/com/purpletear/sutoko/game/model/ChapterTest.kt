package com.purpletear.sutoko.game.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterTest {

    private val nowSeconds = System.currentTimeMillis() / 1000

    @Test
    fun `chapter with past release date is available`() {
        val chapter = Chapter(releaseDate = nowSeconds - 3600)
        assertTrue(chapter.isAvailable)
    }

    @Test
    fun `chapter with future release date is locked`() {
        // Server sends epoch seconds, e.g. story 0AZY0NtFQKu chapter 13 (1818633600).
        val chapter = Chapter(releaseDate = nowSeconds + 3600)
        assertFalse(chapter.isAvailable)
    }

    @Test
    fun `chapter without release date is available`() {
        assertTrue(Chapter(releaseDate = 0L).isAvailable)
    }

    @Test
    fun `extreme release date stays locked without overflow`() {
        assertFalse(Chapter(releaseDate = Long.MAX_VALUE).isAvailable)
    }
}
