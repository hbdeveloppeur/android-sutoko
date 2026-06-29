package com.purpletear.game.data.remote.testing

import com.purpletear.sutoko.game.model.testing.TestEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestEventParserTest {

    @Test
    fun `parses CONNECTED event`() {
        val data = """
            {
                "sessionId": "sess_123",
                "chapterSeeds": {"chapter-1": 5, "chapter-2": 3}
            }
        """.trimIndent()

        val event = TestEventParser.parse("CONNECTED", data) as TestEvent.Connected

        assertEquals("sess_123", event.sessionId)
        assertEquals(mapOf("chapter-1" to 5, "chapter-2" to 3), event.chapterSeeds)
    }

    @Test
    fun `parses SEED_UPDATED event`() {
        val data = """
            {
                "chapterId": "chapter-1",
                "seed": 7,
                "packageUrl": "/test-package/chapter-1/7.zip",
                "changedAssets": ["image.png"]
            }
        """.trimIndent()

        val event = TestEventParser.parse("SEED_UPDATED", data) as TestEvent.SeedUpdated

        assertEquals("chapter-1", event.chapterId)
        assertEquals(7, event.seed)
        assertEquals("/test-package/chapter-1/7.zip", event.packageUrl)
        assertEquals(listOf("image.png"), event.changedAssets)
    }

    @Test
    fun `parses PLAY_FROM_NODE event`() {
        val data = """
            {
                "chapterId": "chapter-1",
                "nodeId": "node-42",
                "seedAtRequest": 7
            }
        """.trimIndent()

        val event = TestEventParser.parse("PLAY_FROM_NODE", data) as TestEvent.PlayFromNode

        assertEquals("chapter-1", event.chapterId)
        assertEquals("node-42", event.nodeId)
        assertEquals(7, event.seedAtRequest)
    }

    @Test
    fun `returns error for unknown event type`() {
        val event = TestEventParser.parse("UNKNOWN", "{}")

        assertTrue(event is TestEvent.Error)
        assertEquals("unknown_event", (event as TestEvent.Error).code)
    }

    @Test
    fun `returns error for malformed json`() {
        val event = TestEventParser.parse("CONNECTED", "not json")

        assertTrue(event is TestEvent.Error)
        assertEquals("parse_error", (event as TestEvent.Error).code)
    }
}
