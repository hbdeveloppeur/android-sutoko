package com.purpletear.game.data.local.entity

import com.purpletear.sutoko.game.model.Chapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterEntityMapperTest {

    @Test
    fun `toEntity preserves available flag`() {
        val chapter = Chapter(
            id = "1",
            number = 2,
            story = "5JZvpvXaS6r",
            code = "2a",
            releaseDate = 1577836800L,
            available = true,
        )

        val entity = chapter.toEntity()

        assertTrue(entity.available)
        assertEquals("2A", entity.code)
        assertEquals(1577836800L, entity.releaseDate)
    }

    @Test
    fun `entity round trip preserves available flag`() {
        val chapter = Chapter(id = "1", available = true)

        assertTrue(chapter.toEntity().toDomain().available)
    }
}
