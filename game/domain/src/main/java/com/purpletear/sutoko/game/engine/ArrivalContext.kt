package com.purpletear.sutoko.game.engine

/**
 * Describes how a node was reached by the engine.
 *
 * This is intentionally a small, focused value object: it carries transient
 * execution context that does not belong in [GameMemory] (which is persisted
 * game state) and that should not leak into the UI layer.
 */
data class ArrivalContext(
    val selectedChoiceNodeId: String? = null,
)
