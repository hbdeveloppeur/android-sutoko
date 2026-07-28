package com.purpletear.sutoko.shop.test

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    val userFlow = MutableStateFlow<User?>(null)
    val isConnectedFlow = MutableStateFlow(false)

    override fun observeUser(): Flow<User?> = userFlow
    override fun observeIsConnected(): Flow<Boolean> = isConnectedFlow
    override fun isConnected(): Result<Boolean> = Result.success(isConnectedFlow.value)
    override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
    override suspend fun disconnect(): Result<Unit> = Result.success(Unit)
}
