package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity

/**
 * Room entity for storing game memory variables.
 * Each row represents one key-value pair for a specific game.
 * 
 * Composite primary key: (gameId, key) - allows multiple games to have
 * the same memory key without collision.
 */
@Keep
@Entity(
    tableName = "game_memories",
    primaryKeys = ["gameId", "key"]
)
data class MemoryEntity(
    val gameId: String,
    val key: String,
    val value: String
)
