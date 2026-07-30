package com.purpletear.sutoko.game.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists the GamePreview menu sound preference across app restarts.
 */
interface GamePreviewSoundRepository {
    /** Observes whether the menu sound is muted. Always emits, defaults to false. */
    fun observeMuted(): Flow<Boolean>

    /** Persists the muted state. */
    suspend fun setMuted(muted: Boolean)
}
