package com.purpletear.aiconversation.data.repository

import com.purpletear.aiconversation.data.BuildConfig
import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.StoryChoiceApi
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.repository.StoryChoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class StoryChoiceRepositoryImpl(private val api: StoryChoiceApi) : StoryChoiceRepository {
    override suspend fun makeChoice(
        userId: String?,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>> = flow {
        val apiResponse = api.makeChoice(
            userId = userId,
            choiceId = choice.id,
            appVersion = BuildConfig.VERSION_NAME,
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let {
                emit(Result.success(Unit))
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