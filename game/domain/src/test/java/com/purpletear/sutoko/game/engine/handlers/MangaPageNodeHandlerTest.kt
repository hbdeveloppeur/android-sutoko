package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MangaPageNodeHandlerTest {

    private val handler = MangaPageNodeHandler(TextProcessorImpl())

    @Test
    fun `substitutes prenom and emits one manga message after a delay`() {
        val memory = createFakeGameMemory().apply {
            set(GameMemory.HERO_NAME_KEY, "Léa")
        }
        val node = Node.MangaPage(
            id = "manga-1",
            imageUrl = "/tmp/games/game1/assets/page.webp",
            messages = listOf(
                Node.MangaPage.MangaMessage(
                    text = "Au revoir [prenom], sache que tu ne seras jamais seul",
                    size = 28f, x = 74.3f, y = 30.4f, w = 22f,
                )
            ),
        )

        val script = handler.buildScript(node, memory)

        assertEquals(2, script.commands.size)
        val delay = script.commands[0] as HandlerCommand.Delay
        assertEquals(520L, delay.millis) // seenMs 0 coerced to the minimum pre-show delay
        val emit = script.commands[1] as HandlerCommand.Emit
        val added = (emit.effect as HandlerEffect.AddMessage).message as GameMessageMangaPage
        assertEquals("/tmp/games/game1/assets/page.webp", added.imageUrl)
        assertEquals(1, added.overlays.size)
        assertEquals(
            "Au revoir Léa, sache que tu ne seras jamais seul",
            added.overlays[0].text
        )
    }

    @Test
    fun `returns empty script for a non manga node`() {
        val memory = createFakeGameMemory()
        val script = handler.buildScript(Node.End("end-1"), memory)
        assertTrue(script.commands.isEmpty())
    }

    @Test
    fun `returns empty script when image url is blank`() {
        val memory = createFakeGameMemory()
        val node = Node.MangaPage(
            id = "manga-1",
            imageUrl = "",
            messages = listOf(Node.MangaPage.MangaMessage("t", 10f, 1f, 1f, 10f)),
        )
        assertTrue(handler.buildScript(node, memory).commands.isEmpty())
    }
}
