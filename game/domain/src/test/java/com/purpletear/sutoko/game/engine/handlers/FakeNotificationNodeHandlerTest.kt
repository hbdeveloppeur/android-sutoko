package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNotificationNodeHandlerTest {

    private val handler = FakeNotificationNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `delay and duration - should delay, emit ShowFakeNotification, then hold for the duration`() {
        val node = Node.FakeNotification(
            id = "n1",
            title = "Kelly J.",
            subtitle = "Tu me manques",
            actionText = "Votre petite amie",
            imageUrl = "/tmp/games/game1/assets/kelly.jpeg",
            characterId = 124,
            delayMs = 1250,
            durationMs = 6000,
        )

        val commands = handler.buildScript(node, memory).commands

        assertEquals(3, commands.size)
        assertEquals(HandlerCommand.Delay(1250), commands[0])
        assertEquals(HandlerCommand.Delay(6000), commands[2])

        val effect = (commands[1] as HandlerCommand.Emit).effect as HandlerEffect.ShowFakeNotification
        assertEquals("Kelly J.", effect.title)
        assertEquals("Tu me manques", effect.subtitle)
        assertEquals("Votre petite amie", effect.actionText)
        assertEquals("/tmp/games/game1/assets/kelly.jpeg", effect.imageUrl)
        assertEquals(124, effect.characterId)
        assertEquals(6000, effect.durationMs)
    }

    @Test
    fun `zero delay and duration - should only emit the effect`() {
        val node = Node.FakeNotification(
            id = "n1",
            title = "T",
            subtitle = "S",
            actionText = "A",
            imageUrl = null,
            characterId = null,
        )

        val commands = handler.buildScript(node, memory).commands

        assertEquals(1, commands.size)
        assertTrue((commands[0] as HandlerCommand.Emit).effect is HandlerEffect.ShowFakeNotification)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Message(id = "m", text = "hello", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }
}
