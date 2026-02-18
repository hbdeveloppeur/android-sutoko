package com.purpletear.aiconversation.data.repository

import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.StyleApi
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.model.Style
import com.purpletear.aiconversation.domain.repository.StyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.Locale

class StyleRepositoryImpl(
    private val api: StyleApi,
) : StyleRepository {
    override suspend fun getAll(): Flow<Result<List<Style>>> = flow {
        val code = Locale.getDefault().language.lowercase().substring(0, 2)
        val apiResponse = api.getAllStyles(locale = code)

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                emit(Result.success(response))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

}