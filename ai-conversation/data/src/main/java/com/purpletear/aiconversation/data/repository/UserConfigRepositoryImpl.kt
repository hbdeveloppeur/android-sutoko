package com.purpletear.aiconversation.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.UserConfigApi
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.repository.UserConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserConfigRepositoryImpl(
    private val api: UserConfigApi,
) : UserConfigRepository {

    override suspend fun updateDeviceToken(
        userId: String,
        userToken: String
    ): Flow<Result<Unit>> = flow {
        val token = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            emit(Result.failure(e))
            return@flow
        }

        try {
            val apiResponse = api.updateDeviceToken(
                userId = userId,
                userToken = userToken,
                deviceToken = token,
            )

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let {
                    emit(Result.success(Unit))
                } ?: run {
                    emit(Result.failure(NoResponseException()))
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}