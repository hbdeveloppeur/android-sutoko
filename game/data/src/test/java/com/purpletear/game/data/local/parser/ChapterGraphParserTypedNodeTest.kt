package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.IntroAlignment
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterGraphParserTypedNodeTest {

    @Test
    fun `code node parses to Node Code and recognises intro markers`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("code-start", "code-message", text = "[intro=start]"),
            node("msg-1", "message", text = "Hello", characterId = 1),
            node("code-end", "code-message", text = "[intro=end]")
        )
        val edges = listOf(
            edge("start-0", "code-start"),
            edge("code-start", "msg-1"),
            edge("msg-1", "code-end")
        )

        val graph = parseGraph(nodes, edges)

        val start = graph.getNode("code-start") as? Node.Code
        assertNotNull(start)
        assertTrue(start!!.isIntroStart)
        assertEquals("[intro=start]", start.sentence)

        val end = graph.getNode("code-end") as? Node.Code
        assertNotNull(end)
        assertTrue(end!!.isIntroEnd)
    }

    @Test
    fun `intro-sentence node parses text alignment delay and duration`() {
        val data = JsonObject().apply {
            addProperty("text", "Once upon a time")
            addProperty("alignment", "top")
            addProperty("delay", 200)
            addProperty("duration", 1500)
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        val graph = parseGraph(nodes, edges)

        val sentence = graph.getNode("line-1") as? Node.IntroSentence
        assertNotNull(sentence)
        assertEquals("Once upon a time", sentence!!.text)
        assertEquals(IntroAlignment.TOP, sentence.alignment)
        assertEquals(200, sentence.delayMs)
        assertEquals(1500, sentence.durationMs)
    }

    @Test
    fun `intro-sentence defaults alignment to center and timings to zero`() {
        val data = JsonObject().apply { addProperty("text", "Hi") }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        val graph = parseGraph(nodes, edges)

        val sentence = graph.getNode("line-1") as Node.IntroSentence
        assertEquals(IntroAlignment.CENTER, sentence.alignment)
        assertEquals(0, sentence.delayMs)
        assertEquals(0, sentence.durationMs)
    }

    @Test
    fun `intro-sentence with unknown alignment fails fast`() {
        val data = JsonObject().apply {
            addProperty("text", "Hi")
            addProperty("alignment", "diagonal")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        assertThrows(IllegalArgumentException::class.java) {
            parseGraph(nodes, edges)
        }
    }

    @Test
    fun `message-theme node parses background and foreground colors`() {
        val data = JsonObject().apply {
            addProperty("backgroundColor", "#FF2200")
            addProperty("foregroundColor", "#00FF00")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("theme-1", "message-theme", data = data)
        )
        val edges = listOf(edge("start-0", "theme-1"))

        val graph = parseGraph(nodes, edges)

        val theme = graph.getNode("theme-1") as? Node.MessageTheme
        assertNotNull(theme)
        assertEquals("#FF2200", theme!!.backgroundColor)
        assertEquals("#00FF00", theme.foregroundColor)
    }

    @Test
    fun `message-theme node normalizes blank colors to null`() {
        val data = JsonObject().apply {
            addProperty("backgroundColor", "   ")
            addProperty("foregroundColor", "")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("theme-1", "message-theme", data = data)
        )
        val edges = listOf(edge("start-0", "theme-1"))

        val graph = parseGraph(nodes, edges)

        val theme = graph.getNode("theme-1") as? Node.MessageTheme
        assertNotNull(theme)
        assertNull(theme!!.backgroundColor)
        assertNull(theme.foregroundColor)
    }

    @Test
    fun `choice-action node is parsed as a player message and keeps the graph connected`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("message-1", "message", text = "Encore heureux", characterId = 78),
            node("action-2", "choice-action", text = "Masser sa cheville", characterId = 78),
            node("narration-3", "narration", text = "Vous vous asseyez")
        )
        val edges = listOf(
            edge("start-0", "message-1"),
            edge("message-1", "action-2"),
            edge("action-2", "narration-3")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "6a")

        val action = graph.getNode("action-2") as Node.Message
        assertEquals("Masser sa cheville", action.text)
        assertEquals(78, action.characterId)
        assertEquals("action-2", graph.getNextEdges("message-1").first().target)
        assertEquals("narration-3", graph.getNextEdges("action-2").first().target)
    }
}
