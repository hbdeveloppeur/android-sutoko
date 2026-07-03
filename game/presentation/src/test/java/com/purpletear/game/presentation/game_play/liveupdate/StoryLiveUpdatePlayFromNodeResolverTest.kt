package com.purpletear.game.presentation.game_play.liveupdate

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryLiveUpdatePlayFromNodeResolverTest {

    @Test
    fun `returns Ready when current graph matches requested chapter`() {
        val graph = ChapterGraph(
            chapterCode = "chapter-1",
            chapterNumber = 1,
            title = "Chapter 1",
            nodes = emptyMap(),
            edges = emptyList(),
            startNodeId = "start"
        )

        val result = StoryLiveUpdatePlayFromNodeResolver.resolve(
            chapterId = "chapter-1",
            currentGraph = graph,
            extractedDirectories = emptyMap()
        )

        assertEquals(PlayFromNodeGraphState.Ready, result)
    }

    @Test
    fun `returns Cached when graph does not match but extracted directory exists`() {
        val graph = ChapterGraph(
            chapterCode = "chapter-2",
            chapterNumber = 2,
            title = "Chapter 2",
            nodes = emptyMap(),
            edges = emptyList(),
            startNodeId = "start"
        )
        val extractedDirectories = mapOf("chapter-1" to "/extracted/chapter-1")

        val result = StoryLiveUpdatePlayFromNodeResolver.resolve(
            chapterId = "chapter-1",
            currentGraph = graph,
            extractedDirectories = extractedDirectories
        )

        assertTrue(result is PlayFromNodeGraphState.Cached)
        assertEquals("/extracted/chapter-1", (result as PlayFromNodeGraphState.Cached).extractedDir)
    }

    @Test
    fun `returns Cached when no graph is loaded and extracted directory exists`() {
        val extractedDirectories = mapOf("chapter-1" to "/extracted/chapter-1")

        val result = StoryLiveUpdatePlayFromNodeResolver.resolve(
            chapterId = "chapter-1",
            currentGraph = null,
            extractedDirectories = extractedDirectories
        )

        assertTrue(result is PlayFromNodeGraphState.Cached)
        assertEquals("/extracted/chapter-1", (result as PlayFromNodeGraphState.Cached).extractedDir)
    }

    @Test
    fun `returns Missing when graph does not match and no extracted directory exists`() {
        val graph = ChapterGraph(
            chapterCode = "chapter-2",
            chapterNumber = 2,
            title = "Chapter 2",
            nodes = emptyMap(),
            edges = emptyList(),
            startNodeId = "start"
        )

        val result = StoryLiveUpdatePlayFromNodeResolver.resolve(
            chapterId = "chapter-1",
            currentGraph = graph,
            extractedDirectories = emptyMap()
        )

        assertEquals(PlayFromNodeGraphState.Missing, result)
    }

    @Test
    fun `returns Missing when no graph and no extracted directory exists`() {
        val result = StoryLiveUpdatePlayFromNodeResolver.resolve(
            chapterId = "chapter-1",
            currentGraph = null,
            extractedDirectories = emptyMap()
        )

        assertEquals(PlayFromNodeGraphState.Missing, result)
    }
}
