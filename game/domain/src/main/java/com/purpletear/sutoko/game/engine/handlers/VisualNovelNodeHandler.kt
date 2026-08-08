package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for visual-novel nodes.
 *
 * The overlay payload is emitted immediately via [HandlerEffect.ShowVisualNovel]; the
 * presentation layer renders it and plays the sounds. Text variables (e.g. `[prenom]`) are
 * resolved via [TextProcessor] before emission, so the presentation layer receives final text.
 *
 * The overlay is a gating interaction: after the effect is emitted, a
 * [HandlerCommand.AwaitVisualNovelDismissal] parks the engine so the next node is not shown
 * until the player dismisses the overlay (see GameEngine.resumeFromVisualNovel).
 *
 * Precondition: [node] is a [Node.VisualNovel] with at least one layer (enforced by the
 * parser). Postcondition: an optional [HandlerCommand.Delay], one [HandlerCommand.Emit] of
 * [HandlerEffect.ShowVisualNovel], then one [HandlerCommand.AwaitVisualNovelDismissal].
 * Returns an empty script if [node] is not a [Node.VisualNovel] or is degenerate.
 */
class VisualNovelNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor,
) : NodeHandler {

    override fun buildScript(node: Node, memory: GameMemory): HandlerScript {
        val visualNovelNode = node as? Node.VisualNovel ?: return HandlerScript()
        if (visualNovelNode.layers.isEmpty()) return HandlerScript()

        val variables = memory.state.value
        val title = visualNovelNode.title
            ?.takeIf { it.isNotBlank() }
            ?.let { textProcessor.process(it, variables) }
        val dialogs = visualNovelNode.dialogs.map { dialog ->
            dialog.copy(text = textProcessor.process(dialog.text, variables))
        }

        GameEngineLogger.d("HAND") {
            "Visual novel ${visualNovelNode.id}: ${visualNovelNode.layers.size} layers, " +
                    "${dialogs.size} dialogs, ${visualNovelNode.sounds.size} sounds"
        }

        val commands = mutableListOf<HandlerCommand>()
        if (visualNovelNode.delayMs > 0) {
            commands += HandlerCommand.Delay(visualNovelNode.delayMs)
        }
        commands += HandlerCommand.Emit(
            HandlerEffect.ShowVisualNovel(
                title = title,
                layers = visualNovelNode.layers,
                dialogs = dialogs,
                sounds = visualNovelNode.sounds,
                theme = visualNovelNode.theme,
            )
        )
        commands += HandlerCommand.AwaitVisualNovelDismissal

        return HandlerScript(commands = commands)
    }
}
