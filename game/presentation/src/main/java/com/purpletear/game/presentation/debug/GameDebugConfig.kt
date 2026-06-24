package com.purpletear.game.presentation.debug

import com.purpletear.game.presentation.BuildConfig

/**
 * Debug-only overrides for game entry points.
 *
 * Add chapter codes here to skip the chapter start and land directly on a specific node.
 * This is useful for reproducing bugs reported by users deep in a chapter.
 *
 * Example:
 * ```
 * val CHAPTER_START_NODE_OVERRIDES: Map<String, String> = mapOf(
 *     "chapter_02" to "node_42"
 * )
 * ```
 *
 * This map is only consulted in debug builds; release builds always start chapters normally.
 */
object GameDebugConfig {
    val CHAPTER_START_NODE_OVERRIDES: Map<String, String> = mapOf(
        // "1A" to "0AZY0NtFQKu-1A-218",
        "2A" to "0AZY0NtFQKu-2A-111",
    )
}

/**
 * Returns the debug override start node for the given chapter, or null if none is configured.
 * Always returns null in release builds.
 */
fun debugStartNodeFor(chapterCode: String): String? {
    return if (BuildConfig.DEBUG) {
        GameDebugConfig.CHAPTER_START_NODE_OVERRIDES[chapterCode]
    } else {
        null
    }
}
