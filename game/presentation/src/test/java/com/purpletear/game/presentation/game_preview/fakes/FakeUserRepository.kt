package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {
    private val user = MutableStateFlow<User?>(null)

    fun setUser(value: User?) {
        user.value = value
    }

    override fun observeUser(): Flow<User?> = user.asStateFlow()
    override fun observeIsConnected(): Flow<Boolean> = flowOf(user.value != null)
    override fun isConnected(): Result<Boolean> = Result.success(user.value != null)
    override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
    override suspend fun disconnect(): Result<Unit> {
        user.value = null
        return Result.success(Unit)
    }
}
