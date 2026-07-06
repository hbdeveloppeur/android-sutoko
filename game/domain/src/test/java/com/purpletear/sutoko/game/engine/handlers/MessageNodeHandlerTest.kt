package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.ArrivalContext
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.model.chapter.ConversationMode
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageNodeHandlerTest {

    private lateinit var handler: MessageNodeHandler
    private lateinit var memory: GameMemory

    @Before
    fun setUp() {
        handler = MessageNodeHandler(TextProcessorImpl())
        memory = createFakeGameMemory().apply {
            setMainCharacterId(1)
        }
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val script = handler.buildScript(Node.Start(id = "start"), memory)

        assertTrue(script.commands.isEmpty())
    }

    @Test
    fun `bracketed text - should skip and return empty script`() {
        val node = Node.Message(id = "msg1", text = "[skip_me]", characterId = 1)

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
    }

    @Test
    fun `blank text after substitution - should return empty script`() {
        val node = Node.Message(id = "msg1", text = "[prenom]", characterId = 1)
        memory.set("heroName", "")

        val script = handler.buildScript(node, memory)

        assertTrue(script.commands.isEmpty())
    }

    @Test
    fun `IRL mode with auto timing - should delay then emit text`() {
        setConversationMode(ConversationMode.IRL)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            isAutoTiming = true
        )

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())

        assertEquals(2, script.commands.size)
        assertEquals(1000L, script.commands[0].delayMillis())
        assertEquals("Hello", script.commands[1].textMessage())
    }

    @Test
    fun `IRL mode manual timing with seenMs - should delay then emit text`() {
        setConversationMode(ConversationMode.IRL)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            isAutoTiming = false,
            seenMs = 750L
        )

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())

        assertEquals(2, script.commands.size)
        assertEquals(750L, script.commands[0].delayMillis())
        assertEquals("Hello", script.commands[1].textMessage())
    }

    @Test
    fun `IRL mode manual timing with zero seenMs - should emit text immediately`() {
        setConversationMode(ConversationMode.IRL)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            isAutoTiming = false,
            seenMs = 0L
        )

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())

        assertEquals(1, script.commands.size)
        assertEquals("Hello", script.commands[0].textMessage())
    }

    @Test
    fun `SMS mode - should produce typing sequence then text`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "msg1", text = "Hello", characterId = 1)

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())

        assertTrue(script.commands.isNotEmpty())
        val firstCommand = script.commands.first()
        assertTrue(
            "First command should emit PlayTypingSound, not a delay",
            firstCommand is HandlerCommand.Emit && firstCommand.effect == HandlerEffect.PlayTypingSound
        )
        assertTrue(script.commands.last().isTextMessage())

        val typingAdds = script.commands.addedTypingMessages()
        val textAdds = script.commands.addedTextMessages()
        val deleteMessages = script.commands.deleteMessages()
        val playTypingSounds = script.commands.playTypingSounds()

        assertEquals(1, typingAdds.size)
        assertEquals(1, textAdds.size)
        assertEquals("Hello", textAdds.first().text)
        assertEquals(1, deleteMessages.size)
        assertTrue(playTypingSounds >= 1)
    }

    @Test
    fun `SMS mode with hesitation - should insert hesitation before main typing`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            isHesitating = true
        )

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())

        val typingAdds = script.commands.addedTypingMessages()
        val deleteMessages = script.commands.deleteMessages()
        val playTypingSounds = script.commands.playTypingSounds()

        assertEquals(2, typingAdds.size)
        assertEquals(2, deleteMessages.size)
        assertTrue(playTypingSounds >= 2)
    }

    @Test
    fun `auto typing duration - should scale with text length within bounds`() {
        setConversationMode(ConversationMode.SMS)
        val shortNode = Node.Message(id = "short", text = "Hi", characterId = 1)
        val longNode = Node.Message(id = "long", text = "A".repeat(100), characterId = 1)

        val shortScript = handler.buildScript(shortNode, memory, previousMessage(), ArrivalContext())
        val longScript = handler.buildScript(longNode, memory, previousMessage(), ArrivalContext())

        val shortDuration = shortScript.commands.filterIsInstance<HandlerCommand.Delay>().maxOf { it.millis }
        val longDuration = longScript.commands.filterIsInstance<HandlerCommand.Delay>().maxOf { it.millis }

        assertEquals(1000L, shortDuration)
        assertEquals(5000L, longDuration)
        assertTrue(longDuration > shortDuration)
    }

    @Test
    fun `manual typing duration - should use node waitMs`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            waitMs = 1234L
        )

        val script = handler.buildScript(node, memory, previousMessage(), ArrivalContext())
        val delays = script.commands.filterIsInstance<HandlerCommand.Delay>().map { it.millis }

        assertTrue(delays.contains(1234L))
    }

    @Test
    fun `first main character message in SMS mode - should skip initial seen delay`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "msg1", text = "Hello", characterId = 1)

        val script = handler.buildScript(node, memory, previousNode = null, ArrivalContext())

        val firstCommand = script.commands.first()
        assertTrue(
            "First command should emit PlayTypingSound, not a delay",
            firstCommand is HandlerCommand.Emit && firstCommand.effect == HandlerEffect.PlayTypingSound
        )
    }

    @Test
    fun `first main character message in IRL mode - should emit text immediately`() {
        setConversationMode(ConversationMode.IRL)
        val node = Node.Message(
            id = "msg1",
            text = "Hello",
            characterId = 1,
            isAutoTiming = true
        )

        val script = handler.buildScript(node, memory, previousNode = null, ArrivalContext())

        assertEquals(1, script.commands.size)
        assertEquals("Hello", script.commands[0].textMessage())
    }

    @Test
    fun `subsequent main character message - should start with typing sound`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "msg1", text = "Hello", characterId = 1)
        val previous = Node.Message(id = "prev", text = "Prev", characterId = 1)

        val script = handler.buildScript(node, memory, previous, ArrivalContext())

        val firstCommand = script.commands.first()
        assertTrue(
            "First command should emit PlayTypingSound, not a delay",
            firstCommand is HandlerCommand.Emit && firstCommand.effect == HandlerEffect.PlayTypingSound
        )
    }

    @Test
    fun `first message from non-main character - should start with typing sound`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "msg1", text = "Hello", characterId = 2)

        val script = handler.buildScript(node, memory, previousNode = null, ArrivalContext())

        val firstCommand = script.commands.first()
        assertTrue(
            "First command should emit PlayTypingSound, not a delay",
            firstCommand is HandlerCommand.Emit && firstCommand.effect == HandlerEffect.PlayTypingSound
        )
    }

    @Test
    fun `SMS mode user choice - should emit text immediately without delays or typing`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "user-choice", text = "My answer", characterId = 1)

        val script = handler.buildScript(
            node,
            memory,
            previousMessage(),
            arrivalContext = ArrivalContext(selectedChoiceNodeId = node.id),
        )

        assertEquals(1, script.commands.size)
        assertEquals("My answer", script.commands[0].textMessage())
        assertTrue(script.commands.none { it is HandlerCommand.Delay })
        assertTrue(script.commands.none { it.addsTypingMessage() })
    }

    @Test
    fun `IRL mode user choice - should emit text immediately without delay`() {
        setConversationMode(ConversationMode.IRL)
        val node = Node.Message(
            id = "user-choice",
            text = "My answer",
            characterId = 1,
            isAutoTiming = true,
        )

        val script = handler.buildScript(
            node,
            memory,
            previousMessage(),
            arrivalContext = ArrivalContext(selectedChoiceNodeId = node.id),
        )

        assertEquals(1, script.commands.size)
        assertEquals("My answer", script.commands[0].textMessage())
        assertTrue(script.commands.none { it is HandlerCommand.Delay })
    }

    @Test
    fun `SMS mode hesitating user choice - should skip hesitation and typing`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(
            id = "user-choice",
            text = "My answer",
            characterId = 1,
            isHesitating = true,
        )

        val script = handler.buildScript(
            node,
            memory,
            previousMessage(),
            arrivalContext = ArrivalContext(selectedChoiceNodeId = node.id),
        )

        assertEquals(1, script.commands.size)
        assertEquals("My answer", script.commands[0].textMessage())
        assertTrue(script.commands.none { it is HandlerCommand.Delay })
        assertTrue(script.commands.none { it.addsTypingMessage() })
    }

    @Test
    fun `main character message after different character - should skip initial delay`() {
        setConversationMode(ConversationMode.SMS)
        val node = Node.Message(id = "msg1", text = "Hello", characterId = 1)
        val previous = Node.Message(id = "prev", text = "Prev", characterId = 2)

        val script = handler.buildScript(node, memory, previous, ArrivalContext())

        val firstCommand = script.commands.first()
        assertTrue(
            "First command should emit PlayTypingSound, not a delay",
            firstCommand is HandlerCommand.Emit && firstCommand.effect == HandlerEffect.PlayTypingSound
        )
    }

    private fun previousMessage(characterId: Int = 1): Node.Message =
        Node.Message(id = "prev", text = "Previous", characterId = characterId)

    private fun setConversationMode(mode: ConversationMode) {
        when (mode) {
            ConversationMode.SMS -> memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
            ConversationMode.IRL -> memory.set(GameMemory.TYPING_ANIMATION_KEY, "false")
        }
    }

    private fun HandlerCommand.delayMillis(): Long = (this as HandlerCommand.Delay).millis

    private fun HandlerCommand.textMessage(): String =
        ((this as HandlerCommand.Emit).effect as HandlerEffect.AddMessage).message.let {
            (it as GameMessageText).text
        }

    private fun HandlerCommand.isDelay(): Boolean = this is HandlerCommand.Delay

    private fun HandlerCommand.isTextMessage(): Boolean {
        val effect = (this as? HandlerCommand.Emit)?.effect as? HandlerEffect.AddMessage ?: return false
        return effect.message is GameMessageText
    }

    private fun List<HandlerCommand>.addedTypingMessages(): List<GameMessageTyping> =
        mapNotNull { command ->
            (command as? HandlerCommand.Emit)
                ?.let { it.effect as? HandlerEffect.AddMessage }
                ?.let { it.message as? GameMessageTyping }
        }

    private fun List<HandlerCommand>.addedTextMessages(): List<GameMessageText> =
        mapNotNull { command ->
            (command as? HandlerCommand.Emit)
                ?.let { it.effect as? HandlerEffect.AddMessage }
                ?.let { it.message as? GameMessageText }
        }

    private fun List<HandlerCommand>.deleteMessages(): List<HandlerEffect.DeleteMessage> =
        mapNotNull { command ->
            (command as? HandlerCommand.Emit)?.effect as? HandlerEffect.DeleteMessage
        }

    private fun List<HandlerCommand>.playTypingSounds(): Int =
        count { command ->
            (command as? HandlerCommand.Emit)?.effect == HandlerEffect.PlayTypingSound
        }

    private fun HandlerCommand.addsTypingMessage(): Boolean {
        val effect = (this as? HandlerCommand.Emit)?.effect as? HandlerEffect.AddMessage ?: return false
        return effect.message is GameMessageTyping
    }
}
