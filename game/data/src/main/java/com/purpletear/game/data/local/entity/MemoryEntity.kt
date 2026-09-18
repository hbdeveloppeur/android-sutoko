package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity for storing game memory variables.
 * Each row retains a key-value pair at the chapter in which it was written.
 *
 * Composite primary key: (gameId, key, chapterNumber) preserves earlier chapter values.
 *
 * The [chapterNumber] column records the chapter in which the memory was written.
 * It is used to discard stale state when the user replays an earlier chapter.
 * A default of [Int.MAX_VALUE] is used for legacy rows so they are treated as
 * "future" state and cleaned up on the next chapter load.
 */
@Keep
@Entity(
    tableName = "game_memories",
    primaryKeys = ["gameId", "key", "chapterNumber"]
)
data class MemoryEntity(
    val gameId: String,
    val key: String,
    val value: String,
    @ColumnInfo(defaultValue = "2147483647")
    val chapterNumber: Int = Int.MAX_VALUE
)
