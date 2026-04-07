package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node

/**
 * Context needed for node execution.
 * Encapsulates all dependencies to avoid long parameter lists in GameEngine.
 */
internal data class ExecutionContext(
    val graph: ChapterGraph,
    val node: Node,
    val nodeId: String,
    val handler: NodeHandler
)
