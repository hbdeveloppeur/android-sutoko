package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundNodeHandlerTest {

    private val handler = SoundNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `sound node with loop false - should emit PlaySound with loop false`() {
        val node = Node.Sound(
            id = "sound-1",
            soundUrl = "/path/knife.mp3",
            loop = false
        )

        val script = handler.buildScript(node, memory)

        assertEquals(1, script.commands.size)
        val effect = (script.commands[0] as HandlerCommand.Emit).effect
        assertTrue(effect is HandlerEffect.PlaySound)
        assertEquals("/path/knife.mp3", (effect as HandlerEffect.PlaySound).soundUrl)
        assertEquals(false, effect.loop)
    }

    @Test
    fun `sound node with loop true - should emit PlaySound with loop true`() {
        val node = Node.Sound(
            id = "sound-2",
            soundUrl = "/path/ambient.mp3",
            loop = true
        )

        val script = handler.buildScript(node, memory)

        assertEquals(1, script.commands.size)
        val effect = (script.commands[0] as HandlerCommand.Emit).effect
        assertTrue(effect is HandlerEffect.PlaySound)
        assertEquals("/path/ambient.mp3", (effect as HandlerEffect.PlaySound).soundUrl)
        assertEquals(true, effect.loop)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Message(id = "msg-1", text = "hello", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertEquals(null, script.nextNodeId)
    }
}
