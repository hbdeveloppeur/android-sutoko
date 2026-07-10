package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for manga-page nodes.
 *
 * Timing mirrors [MessageImageNodeHandler]: a pre-show delay, then the message is
 * emitted. Text variables (e.g. `[prenom]`) are resolved via [TextProcessor] before
 * the message is emitted, so the presentation layer receives final text.
 *
 * Precondition: [node] is a [Node.MangaPage] with a non-blank image and at least one
 * message (enforced by the parser). Postcondition: exactly one [HandlerCommand.Delay]
 * followed by one [HandlerEffect.AddMessage] carrying a [GameMessageMangaPage]. Returns
 * an empty script if [node] is not a [Node.MangaPage] or is degenerate.
 */
class MangaPageNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor,
) : NodeHandler {

    override fun buildScript(node: Node, memory: GameMemory): HandlerScript {
        val mangaNode = node as? Node.MangaPage ?: return HandlerScript()
        if (mangaNode.imageUrl.isBlank() || mangaNode.messages.isEmpty()) return HandlerScript()

        val variables = memory.state.value
        val overlays = mangaNode.messages.map { message ->
            GameMessageMangaPage.TextOverlay(
                text = textProcessor.process(message.text, variables),
                size = message.size,
                x = message.x,
                y = message.y,
                w = message.w,
            )
        }

        GameEngineLogger.d("HAND") {
            "Manga page ${mangaNode.id}: ${mangaNode.imageUrl} (${overlays.size} overlays)"
        }

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Delay(mangaNode.seenMs.coerceAtLeast(MIN_PRE_SHOW_DELAY_MS)),
                HandlerCommand.Emit(
                    HandlerEffect.AddMessage(
                        GameMessageMangaPage(
                            id = UUID.randomUUID().toString(),
                            imageUrl = mangaNode.imageUrl,
                            overlays = overlays,
                        )
                    )
                )
            )
        )
    }

    private companion object {
        private const val MIN_PRE_SHOW_DELAY_MS = 520L
    }
}
