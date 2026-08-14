package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's [StoryAdvanceMode] across app restarts.
 *
 * The stored value is an explicit override: until the player picks a mode, callers resolve
 * one with [StoryAdvanceMode.defaultFor] based on the story being played.
 */
interface StoryAdvanceModeRepository {
    /** Observes the explicitly chosen mode, or null when the player never picked one. */
    fun observeExplicit(): Flow<StoryAdvanceMode?>

    /** Persists [mode] as the explicit player choice. */
    suspend fun set(mode: StoryAdvanceMode)
}
