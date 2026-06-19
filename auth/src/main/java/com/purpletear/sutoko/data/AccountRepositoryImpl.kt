package com.purpletear.sutoko.data

import com.purpletear.sutoko.data.remote.AccountApi
import com.purpletear.sutoko.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.sutoko.domain.repository.AccountRepository
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val api: AccountApi,
) : AccountRepository {

    override suspend fun requestAccountDeletion(userId: String): Result<Unit> {
        return try {
            val response = api.requestAccountDeletion(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ApiFailureResponseHandler.handler(response.errorBody()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
