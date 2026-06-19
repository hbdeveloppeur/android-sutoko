package com.purpletear.sutoko.domain.repository

interface AccountRepository {
    suspend fun requestAccountDeletion(userId: String): Result<Unit>
}
