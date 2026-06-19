package com.purpletear.sutoko.data

import com.google.firebase.messaging.FirebaseMessaging
import com.purpletear.sutoko.data.exception.NoResponseException
import com.purpletear.sutoko.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.sutoko.data.remote.UserConfigApi
import com.purpletear.sutoko.domain.repository.UserConfigRepository
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserConfigRepositoryImpl(
    private val api: UserConfigApi,
    private val userRepository: UserRepository,
) : UserConfigRepository {

    override suspend fun updateDeviceToken(): Flow<Result<Unit>> = flow {
        val user = userRepository.observeUser().first()
        if (user == null) {
            emit(Result.failure(IllegalStateException("No authenticated user available")))
            return@flow
        }

        val token = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            emit(Result.failure(e))
            return@flow
        }

        try {
            val apiResponse = api.updateDeviceToken(
                userId = user.id,
                userToken = user.token,
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