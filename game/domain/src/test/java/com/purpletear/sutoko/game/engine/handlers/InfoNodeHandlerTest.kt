package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.ArrivalContext
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InfoNodeHandlerTest {

    private val handler = InfoNodeHandler(TextProcessorImpl())
    private val memory = createFakeGameMemory()

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Message(id = "msg-1", text = "hello", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertEquals(null, script.nextNodeId)
    }

    @Test
    fun `first info node - should delay 280ms then emit info`() {
        val node = Node.Info(id = "info-1", text = "Narration text")

        val script = handler.buildScript(node, memory, previousNode = null, ArrivalContext())

        assertEquals(2, script.commands.size)
        assertEquals(280L, (script.commands[0] as HandlerCommand.Delay).millis)
        assertEquals(
            "Narration text",
            script.commands[1].infoText()
        )
    }

    @Test
    fun `subsequent info node - should delay 2000ms then emit info`() {
        val node = Node.Info(id = "info-1", text = "Narration text")
        val previous = Node.Message(id = "prev", text = "Previous", characterId = 1)

        val script = handler.buildScript(node, memory, previousNode = previous, ArrivalContext())

        assertEquals(2, script.commands.size)
        assertEquals(2000L, (script.commands[0] as HandlerCommand.Delay).millis)
        assertEquals(
            "Narration text",
            script.commands[1].infoText()
        )
    }

    private fun HandlerCommand.infoText(): String {
        val effect = (this as HandlerCommand.Emit).effect as HandlerEffect.AddMessage
        return (effect.message as GameMessageInfo).text
    }
}
