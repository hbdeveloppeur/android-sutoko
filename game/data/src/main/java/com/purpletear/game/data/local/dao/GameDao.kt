package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.purpletear.game.data.local.entity.GameCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE isOfficial = 1")
    fun observeOfficialGames(): Flow<List<GameCatalogEntity>>

    @Query("SELECT * FROM games WHERE isOfficial = 0")
    fun observeUserGames(): Flow<List<GameCatalogEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun observeGame(id: String): Flow<GameCatalogEntity?>

    @Query("DELETE FROM games WHERE isOfficial = 1")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertAll(entities: List<GameCatalogEntity>)

    @Transaction
    suspend fun replaceAll(entities: List<GameCatalogEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}
