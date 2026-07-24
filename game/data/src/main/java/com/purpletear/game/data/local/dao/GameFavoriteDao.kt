package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.game.data.local.entity.GameFavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for favorite game records.
 */
@Dao
interface GameFavoriteDao {
    @Query("SELECT gameId FROM game_favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM game_favorites WHERE gameId = :gameId)")
    suspend fun exists(gameId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: GameFavoriteEntity)

    @Query("DELETE FROM game_favorites WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
