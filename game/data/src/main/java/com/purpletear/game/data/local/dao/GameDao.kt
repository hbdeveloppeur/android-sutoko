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

    // Never evict an installed game: the server listings are paginated and a
    // locally installed story may be absent from the fetched page(s).
    @Query("DELETE FROM games WHERE isOfficial = 1 AND id NOT IN (SELECT gameId FROM game_installs)")
    suspend fun deleteAllOfficial()

    @Query("DELETE FROM games WHERE isOfficial = 0 AND id NOT IN (SELECT gameId FROM game_installs)")
    suspend fun deleteAllUserGames()

    @Upsert
    suspend fun upsertAll(entities: List<GameCatalogEntity>)

    @Transaction
    suspend fun replaceAllOfficial(entities: List<GameCatalogEntity>) {
        deleteAllOfficial()
        upsertAll(entities)
    }

    @Transaction
    suspend fun replaceAllUserGames(entities: List<GameCatalogEntity>) {
        deleteAllUserGames()
        upsertAll(entities)
    }
}
