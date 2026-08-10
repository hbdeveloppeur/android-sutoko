package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's [StoryAdvanceMode] across app restarts.
 */
interface StoryAdvanceModeRepository {
    /** Observes the current mode. Always emits, defaults to [StoryAdvanceMode.AUTO_PLAY]. */
    fun observe(): Flow<StoryAdvanceMode>

    /** Returns the current mode, [StoryAdvanceMode.AUTO_PLAY] when never set. */
    suspend fun get(): StoryAdvanceMode

    /** Persists [mode]. */
    suspend fun set(mode: StoryAdvanceMode)
}
