package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

/**
 * A single persisted memory value together with the chapter number in which it was set.
 *
 * The chapter number is used to prevent overlap when a user replays an earlier chapter:
 * memories written in the target chapter or any later chapter are removed before the
 * chapter starts.
 */
@Keep
data class MemoryEntry(
    val value: String,
    val chapterNumber: Int
)
