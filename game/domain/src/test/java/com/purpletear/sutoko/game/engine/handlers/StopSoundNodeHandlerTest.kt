package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StopSoundNodeHandlerTest {

    private val handler = StopSoundNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `stop-sound node - should emit StopSound with the target node id`() {
        val node = Node.StopSound(id = "stop-1", targetNodeId = "sound-1786241134869")

        val script = handler.buildScript(node, memory)

        val effects = script.commands.filterIsInstance<HandlerCommand.Emit>().map { it.effect }
        assertEquals(listOf(HandlerEffect.StopSound("sound-1786241134869")), effects)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Sound(id = "s", soundUrl = "bg.mp3")

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }
}
