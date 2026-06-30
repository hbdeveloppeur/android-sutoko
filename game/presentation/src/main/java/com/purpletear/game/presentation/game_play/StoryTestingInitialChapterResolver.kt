package com.purpletear.game.presentation.game_play

import com.purpletear.sutoko.game.testing.StoryTestingLogger

/**
 * Decides which chapter the phone should start testing first.
 *
 * The resolution is deterministic and logged so developers can audit why a session started
 * from a particular chapter.
 */
internal object StoryTestingInitialChapterResolver {

    /**
     * Returns the chapter UUID the test session should start from.
     *
     * Resolution order:
     * 1. [lastWorkedOnChapterId] if it is present in [chapterSeeds].
     * 2. The chapter with the highest seed (proxy for "most recently published").
     * 3. The first chapter in [chapterSeeds] insertion order (last resort).
     *
     * @param chapterSeeds Map of chapter UUID -> latest server seed.
     * @param lastWorkedOnChapterId Chapter UUID persisted from the author's previous session.
     */
    fun resolve(chapterSeeds: Map<String, Int>, lastWorkedOnChapterId: String?): String {
        require(chapterSeeds.isNotEmpty()) { "chapterSeeds must not be empty" }

        if (lastWorkedOnChapterId != null && chapterSeeds.containsKey(lastWorkedOnChapterId)) {
            StoryTestingLogger.i("SYNC") { "Using last-tested chapter from memory — $lastWorkedOnChapterId" }
            return lastWorkedOnChapterId
        }

        val highestSeed = chapterSeeds.maxByOrNull { it.value }
        if (highestSeed != null) {
            StoryTestingLogger.i("SYNC") { "No last-tested chapter; using highest seed — ${highestSeed.key} (${highestSeed.value})" }
            return highestSeed.key
        }

        val first = chapterSeeds.keys.first()
        StoryTestingLogger.i("SYNC") { "Falling back to first chapter — $first" }
        return first
    }
}
