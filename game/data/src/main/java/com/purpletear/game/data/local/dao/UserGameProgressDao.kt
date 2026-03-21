package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.sutoko.game.model.UserGameProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameProgressDao {
    @Query("SELECT * FROM user_game_progress WHERE gameId = :gameId")
    fun observe(gameId: String): Flow<UserGameProgressEntity?>

    @Query("SELECT * FROM user_game_progress WHERE gameId = :gameId")
    suspend fun get(gameId: String): UserGameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(progress: UserGameProgressEntity)

    @Query("DELETE FROM user_game_progress WHERE gameId = :gameId")
    suspend fun delete(gameId: String)
}
