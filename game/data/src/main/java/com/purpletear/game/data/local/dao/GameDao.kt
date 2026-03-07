package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.sutoko.game.model.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getOfficialGames(gameId: String): List<Game>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Game>)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: String): Game?

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun delete(gameId: String)
}
