package com.purpletear.ai_conversation.data.repository

import com.purpletear.ai_conversation.data.BuildConfig
import com.purpletear.ai_conversation.data.remote.VersionApi
import com.purpletear.ai_conversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.ai_conversation.domain.model.VersionResponse
import com.purpletear.ai_conversation.domain.repository.VersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class VersionRepositoryImpl(
    private val api: VersionApi,
) : VersionRepository {
    override suspend fun getVersionInfo(): Flow<Result<VersionResponse>> = flow {
        val apiResponse = api.getVersion(
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                emit(Result.success(response))
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            exception.printStackTrace()
        }

    }.catch {
        emit(Result.failure(it))
    }
}