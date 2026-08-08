package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualNovelNodeHandlerTest {

    private val handler = VisualNovelNodeHandler(TextProcessorImpl())

    private fun visualNovelNode(
        delayMs: Long = 0,
        layers: List<Node.VisualNovel.Layer> = listOf(
            Node.VisualNovel.Layer(path = "/tmp/games/game1/assets/bg.webp", assetId = 12)
        ),
    ) = Node.VisualNovel(
        id = "vn-1",
        title = "Inconnue [prenom]",
        layers = layers,
        dialogs = listOf(
            Node.VisualNovel.Dialog(text = "Hé [prenom] ?", durationMs = 2500),
            Node.VisualNovel.Dialog(text = "Tu m'écoutes ?", durationMs = null),
        ),
        sounds = listOf(
            Node.VisualNovel.Sound(path = "/tmp/games/game1/assets/wind.mp3", volume = 0.6f, loop = true)
        ),
        theme = Node.VisualNovel.Theme(colorHex = "#CD40CD", opacity = 0.7f),
        delayMs = delayMs,
    )

    @Test
    fun `substitutes variables, emits ShowVisualNovel then parks for dismissal`() {
        val memory = createFakeGameMemory().apply {
            set(GameMemory.HERO_NAME_KEY, "Léa")
        }

        val script = handler.buildScript(visualNovelNode(), memory)

        assertEquals(2, script.commands.size)
        val emit = script.commands[0] as HandlerCommand.Emit
        val effect = emit.effect as HandlerEffect.ShowVisualNovel
        assertEquals("Inconnue Léa", effect.title)
        assertEquals("Hé Léa ?", effect.dialogs[0].text)
        assertEquals(2500L, effect.dialogs[0].durationMs)
        assertEquals(null, effect.dialogs[1].durationMs)
        assertEquals(1, effect.layers.size)
        assertEquals(1, effect.sounds.size)
        assertEquals("#CD40CD", effect.theme.colorHex)
        assertEquals(HandlerCommand.AwaitVisualNovelDismissal, script.commands[1])
    }

    @Test
    fun `prepends a delay command when delayMs is positive`() {
        val script = handler.buildScript(visualNovelNode(delayMs = 1250), createFakeGameMemory())

        assertEquals(3, script.commands.size)
        assertEquals(HandlerCommand.Delay(1250L), script.commands[0])
        assertTrue(script.commands[1] is HandlerCommand.Emit)
        assertEquals(HandlerCommand.AwaitVisualNovelDismissal, script.commands[2])
    }

    @Test
    fun `returns empty script for a non visual-novel node`() {
        val script = handler.buildScript(Node.End("end-1"), createFakeGameMemory())
        assertTrue(script.commands.isEmpty())
    }

    @Test
    fun `returns empty script when there is no layer`() {
        val script = handler.buildScript(visualNovelNode(layers = emptyList()), createFakeGameMemory())
        assertTrue(script.commands.isEmpty())
    }
}
