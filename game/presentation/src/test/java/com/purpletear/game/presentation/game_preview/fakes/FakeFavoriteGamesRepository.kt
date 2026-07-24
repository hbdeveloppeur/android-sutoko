package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFavoriteGamesRepository : FavoriteGamesRepository {
    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    fun setFavorites(ids: Set<String>) {
        favoriteIds.value = ids
    }

    override fun observeFavoriteIds(): Flow<Set<String>> = favoriteIds.asStateFlow()

    override suspend fun toggle(gameId: String) {
        favoriteIds.value = if (gameId in favoriteIds.value) {
            favoriteIds.value - gameId
        } else {
            favoriteIds.value + gameId
        }
    }
}
