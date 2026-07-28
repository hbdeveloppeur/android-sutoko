package com.purpletear.sutoko.game.model

import org.junit.Assert.assertFalse
import org.junit.Test

class ChapterTest {

    private val nowSeconds = System.currentTimeMillis() / 1000


    @Test
    fun `chapter with future release date is locked`() {
        val chapter = Chapter(releaseDate = nowSeconds + 3600)
        assertFalse(chapter.available)
    }

    @Test
    fun `extreme release date stays locked without overflow`() {
        assertFalse(Chapter(releaseDate = Long.MAX_VALUE).available)
    }
}
