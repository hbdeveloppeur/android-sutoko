package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserGameProgressRepository : UserGameProgressRepository {
    private val storage = mutableMapOf<String, MutableStateFlow<UserGameProgress>>()

    override fun observe(gameId: String): Flow<UserGameProgress> {
        return storage.getOrPut(gameId) { MutableStateFlow(UserGameProgress(gameId = gameId)) }.asStateFlow()
    }

    override suspend fun get(gameId: String): UserGameProgress {
        return storage.getOrPut(gameId) { MutableStateFlow(UserGameProgress(gameId = gameId)) }.value
    }

    override suspend fun save(progress: UserGameProgress) {
        storage.getOrPut(progress.gameId) { MutableStateFlow(progress) }.value = progress
    }

    override suspend fun delete(gameId: String) {
        storage.remove(gameId)
    }
}
