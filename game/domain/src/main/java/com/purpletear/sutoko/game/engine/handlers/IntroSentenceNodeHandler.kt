package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * No-op handler for `intro-sentence` nodes.
 *
 * Intro sentences are rendered exclusively by `CinematicScreen` from the extracted cinematic body;
 * the SMS engine never traverses them during normal play. If one is reached accidentally (authoring
 * error — e.g. a sentence outside a cinematic), this handler does nothing and lets the engine
 * continue to the next node rather than crashing.
 */
class IntroSentenceNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(node: Node, memory: GameMemory): HandlerScript = HandlerScript()
}
