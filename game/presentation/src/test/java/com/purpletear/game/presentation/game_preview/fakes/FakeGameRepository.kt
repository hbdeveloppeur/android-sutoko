package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeGameRepository : GameRepository {
    private val games = mutableMapOf<String, MutableStateFlow<GameCatalog?>>()
    private val errors = mutableMapOf<String, Throwable>()
    private val downloadLinks = mutableMapOf<String, Result<String>>()

    fun setGame(id: String, catalog: GameCatalog?) {
        games.getOrPut(id) { MutableStateFlow(null) }.value = catalog
        errors.remove(id)
    }

    fun setError(id: String, error: Throwable) {
        errors[id] = error
    }

    fun setDownloadLink(gameId: String, result: Result<String>) {
        downloadLinks[gameId] = result
    }

    override fun observeGame(id: String): Flow<GameCatalog?> {
        errors[id]?.let { error ->
            return kotlinx.coroutines.flow.flow { throw error }
        }
        return games.getOrPut(id) { MutableStateFlow(null) }.asStateFlow()
    }

    override suspend fun getDownloadLink(
        gameId: String,
        userId: String?,
        userToken: String?
    ): Result<String> {
        return downloadLinks[gameId] ?: Result.failure(IllegalStateException("No download link set for $gameId"))
    }

    override fun observeOfficialGames(): Flow<List<GameCatalog>> = emptyFlow()
    override fun observeUserGames(): Flow<List<GameCatalog>> = emptyFlow()
    override suspend fun syncOfficialGames(languageTag: String): Result<Unit> = Result.success(Unit)
    override suspend fun syncUserGames(languageTag: String): Result<Unit> = Result.success(Unit)
    override suspend fun loadMoreUserGames(languageTag: String): Result<Boolean> = Result.success(false)
    override suspend fun searchStories(
        query: String,
        languageTag: String,
        page: Int,
        limit: Int
    ): Result<List<GameCatalog>> = Result.success(emptyList())

    override suspend fun getOneUserGames(
        userId: String,
        page: Int,
        limit: Int
    ): Result<List<GameCatalog>> = Result.success(emptyList())
}
