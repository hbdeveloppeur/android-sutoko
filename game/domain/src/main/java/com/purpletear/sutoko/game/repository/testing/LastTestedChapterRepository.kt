package com.purpletear.sutoko.game.repository.testing

/**
 * Persists the chapter the author last asked to test.
 *
 * The value is keyed by [storyId] so each story remembers its own last-tested chapter
 * independently. A null return value means the author has not tested this story yet
 * (or the persisted value has been cleared).
 */
interface LastTestedChapterRepository {

    /**
     * Returns the last-tested chapter UUID for [storyId], or null if none is stored.
     */
    suspend fun get(storyId: String): String?

    /**
     * Stores [chapterId] as the last-tested chapter for [storyId].
     */
    suspend fun set(storyId: String, chapterId: String)
}
