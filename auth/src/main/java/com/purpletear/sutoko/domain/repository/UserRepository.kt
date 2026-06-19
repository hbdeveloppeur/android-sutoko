package com.purpletear.sutoko.domain.repository

import com.purpletear.sutoko.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(): Flow<User?>
    fun observeIsConnected(): Flow<Boolean>
    fun isConnected(): Result<Boolean>
    suspend fun connect(id: String, token: String): Result<Unit>
    suspend fun disconnect(): Result<Unit>
}
