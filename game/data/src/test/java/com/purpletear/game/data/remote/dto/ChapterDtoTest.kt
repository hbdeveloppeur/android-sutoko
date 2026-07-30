package com.purpletear.game.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterDtoTest {

    private val gson = Gson()

    @Test
    fun `layout sides right is mapped to domain rightSideCharacterIds`() {
        val json = """
            {
              "id": "01KRCFS81JQD19SAZJABMVBDRF",
              "number": 1,
              "alternative": "A",
              "createdAt": 1778535669,
              "releaseDate": 1641060000,
              "available": true,
              "story": "yfRAK4g1ifH",
              "metas": {"id": 1, "lang": "fr-FR", "title": "L'homme Clown", "description": "Emma"},
              "canvasAppVersion": 1,
              "layout": {"sides": {"right": [73]}},
              "code": "1a"
            }
        """.trimIndent()

        val chapter = gson.fromJson(json, ChapterDto::class.java).toDomain()

        assertEquals(listOf(73), chapter.rightSideCharacterIds)
    }

    @Test
    fun `missing layout maps to empty rightSideCharacterIds`() {
        val json = """
            {
              "id": "1",
              "number": 1,
              "alternative": "A",
              "createdAt": 1,
              "releaseDate": 1,
              "available": true,
              "story": "story",
              "metas": {"id": 1, "lang": "fr-FR", "title": "t", "description": "d"},
              "canvasAppVersion": 1,
              "code": "1a"
            }
        """.trimIndent()

        val chapter = gson.fromJson(json, ChapterDto::class.java).toDomain()

        assertTrue(chapter.rightSideCharacterIds.isEmpty())
    }

    @Test
    fun `layout without right side maps to empty rightSideCharacterIds`() {
        val json = """
            {
              "id": "1",
              "number": 1,
              "alternative": "A",
              "createdAt": 1,
              "releaseDate": 1,
              "available": true,
              "story": "story",
              "metas": {"id": 1, "lang": "fr-FR", "title": "t", "description": "d"},
              "canvasAppVersion": 1,
              "layout": {"sides": {}},
              "code": "1a"
            }
        """.trimIndent()

        val chapter = gson.fromJson(json, ChapterDto::class.java).toDomain()

        assertTrue(chapter.rightSideCharacterIds.isEmpty())
    }
}
