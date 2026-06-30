package com.purpletear.game.presentation.game_play

import org.junit.Assert.assertEquals
import org.junit.Test

class StoryTestingInitialChapterResolverTest {

    @Test
    fun `returns last worked on chapter when present in seeds`() {
        val seeds = mapOf("chapter-1" to 1, "chapter-2" to 5)

        val result = StoryTestingInitialChapterResolver.resolve(seeds, "chapter-1")

        assertEquals("chapter-1", result)
    }

    @Test
    fun `falls back to highest seed when last worked on is missing`() {
        val seeds = mapOf("chapter-1" to 1, "chapter-2" to 5, "chapter-3" to 2)

        val result = StoryTestingInitialChapterResolver.resolve(seeds, "chapter-missing")

        assertEquals("chapter-2", result)
    }

    @Test
    fun `falls back to highest seed when last worked on is null`() {
        val seeds = mapOf("chapter-1" to 3, "chapter-2" to 1)

        val result = StoryTestingInitialChapterResolver.resolve(seeds, null)

        assertEquals("chapter-1", result)
    }

    @Test
    fun `returns the only chapter when seeds has a single entry`() {
        val seeds = mapOf("chapter-1" to 1)

        val result = StoryTestingInitialChapterResolver.resolve(seeds, null)

        assertEquals("chapter-1", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when seeds is empty`() {
        StoryTestingInitialChapterResolver.resolve(emptyMap(), null)
    }
}
