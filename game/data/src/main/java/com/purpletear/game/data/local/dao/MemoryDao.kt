package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.game.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for game memory operations.
 */
@Dao
interface MemoryDao {
    
    /**
     * Gets all memories for a specific game.
     */
    @Query("SELECT * FROM game_memories WHERE gameId = :gameId")
    suspend fun getAllForGame(gameId: String): List<MemoryEntity>

    /**
     * Observes all memories for a specific game.
     */
    @Query("SELECT * FROM game_memories WHERE gameId = :gameId")
    fun observeAllForGame(gameId: String): Flow<List<MemoryEntity>>
    
    /**
     * Inserts or replaces a memory entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)
    
    /**
     * Inserts or replaces multiple memory entries.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)
    
    /**
     * Deletes all memories for a specific game.
     */
    @Query("DELETE FROM game_memories WHERE gameId = :gameId")
    suspend fun deleteAllForGame(gameId: String)
    
    /**
     * Deletes a specific memory entry.
     */
    @Query("DELETE FROM game_memories WHERE gameId = :gameId AND key = :key")
    suspend fun delete(gameId: String, key: String)
}
