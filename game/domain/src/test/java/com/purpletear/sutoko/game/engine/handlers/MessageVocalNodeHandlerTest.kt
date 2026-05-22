package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.GameMessageVocal
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageVocalNodeHandlerTest {

    private val handler = MessageVocalNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `message vocal with text - should emit AddMessage and PlayVocal`() {
        val node = Node.MessageVocal(
            id = "vocal-1",
            audioUrl = "/path/voice.mp3",
            characterId = 6,
        )

        val script = handler.buildScript(node, memory)

        assertEquals(2, script.commands.size)

        val firstEffect = (script.commands[0] as HandlerCommand.Emit).effect
        assertTrue(firstEffect is HandlerEffect.AddMessage)
        val message = (firstEffect as HandlerEffect.AddMessage).message
        assertTrue(message is GameMessageVocal)
        assertEquals("/path/voice.mp3", (message as GameMessageVocal).audioUrl)
        assertEquals(6, message.characterId)
        assertEquals(GameMessageType.Vocal, message.type)

        val secondEffect = (script.commands[1] as HandlerCommand.Emit).effect
        assertTrue(secondEffect is HandlerEffect.PlayVocal)
        assertEquals("/path/voice.mp3", (secondEffect as HandlerEffect.PlayVocal).audioUrl)
    }

    @Test
    fun `message vocal with blank text - should emit AddMessage and PlayVocal`() {
        val node = Node.MessageVocal(
            id = "vocal-2",
            audioUrl = "/path/voice2.mp3",
            characterId = 6,
        )

        val script = handler.buildScript(node, memory)

        assertEquals(2, script.commands.size)

        val firstEffect = (script.commands[0] as HandlerCommand.Emit).effect
        assertTrue(firstEffect is HandlerEffect.AddMessage)
        val message = (firstEffect as HandlerEffect.AddMessage).message
        assertTrue(message is GameMessageVocal)
        assertEquals("/path/voice2.mp3", (message as GameMessageVocal).audioUrl)
        assertEquals(6, message.characterId)

        val secondEffect = (script.commands[1] as HandlerCommand.Emit).effect
        assertTrue(secondEffect is HandlerEffect.PlayVocal)
        assertEquals("/path/voice2.mp3", (secondEffect as HandlerEffect.PlayVocal).audioUrl)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val node = Node.Message(id = "msg-1", text = "hello", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
        assertEquals(null, script.nextNodeId)
    }
}
