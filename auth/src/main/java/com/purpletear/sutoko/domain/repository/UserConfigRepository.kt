package com.purpletear.sutoko.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserConfigRepository {
    suspend fun updateDeviceToken(): Flow<Result<Unit>>
}
