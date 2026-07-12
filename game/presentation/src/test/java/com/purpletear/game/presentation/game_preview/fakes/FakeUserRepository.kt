package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserRepository : UserRepository {
    private val user = MutableStateFlow<User?>(null)
    private val isConnected = MutableStateFlow(false)

    fun setUser(value: User?) {
        user.value = value
        isConnected.value = value != null
    }

    override fun observeUser(): Flow<User?> = user.asStateFlow()
    override fun observeIsConnected(): Flow<Boolean> = isConnected.asStateFlow()
    override fun isConnected(): Result<Boolean> = Result.success(user.value != null)
    override suspend fun connect(id: String, token: String): Result<Unit> {
        setUser(User(id, token))
        return Result.success(Unit)
    }

    override suspend fun disconnect(): Result<Unit> {
        setUser(null)
        return Result.success(Unit)
    }
}
