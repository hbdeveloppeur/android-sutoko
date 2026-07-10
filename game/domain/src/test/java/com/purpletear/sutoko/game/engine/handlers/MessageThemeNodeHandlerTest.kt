package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageThemeNodeHandlerTest {

    private val handler = MessageThemeNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `both colors - should emit UpdateMemory for background and foreground`() {
        val node = Node.MessageTheme(id = "t", backgroundColor = "#FF2200", foregroundColor = "#00FF00")

        val updates = handler.buildScript(node, memory).memoryUpdates()

        assertEquals(2, updates.size)
        assertEquals("#FF2200", updates[GameMemory.MESSAGE_THEME_BG_KEY])
        assertEquals("#00FF00", updates[GameMemory.MESSAGE_THEME_FG_KEY])
    }

    @Test
    fun `only background - should emit a single UpdateMemory for background`() {
        val node = Node.MessageTheme(id = "t", backgroundColor = "#FF2200", foregroundColor = null)

        val updates = handler.buildScript(node, memory).memoryUpdates()

        assertEquals(1, updates.size)
        assertEquals("#FF2200", updates[GameMemory.MESSAGE_THEME_BG_KEY])
        assertNull(updates[GameMemory.MESSAGE_THEME_FG_KEY])
    }

    @Test
    fun `no colors - should return empty script`() {
        val node = Node.MessageTheme(id = "t", backgroundColor = null, foregroundColor = null)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Message(id = "m", text = "hello", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }

    private fun com.purpletear.sutoko.game.engine.HandlerScript.memoryUpdates(): Map<String, String> =
        commands
            .filterIsInstance<HandlerCommand.Emit>()
            .map { it.effect }
            .filterIsInstance<HandlerEffect.UpdateMemory>()
            .associate { it.key to it.value }
}
