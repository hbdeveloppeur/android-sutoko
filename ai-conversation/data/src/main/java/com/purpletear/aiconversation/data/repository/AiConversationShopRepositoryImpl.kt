package com.purpletear.aiconversation.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.purpletear.aiconversation.data.BuildConfig
import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.ShopApi
import com.purpletear.aiconversation.data.remote.dto.toDomain
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.model.AiMessagePack
import com.purpletear.aiconversation.domain.model.AiTokensState
import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import purpletear.fr.purpleteartools.GameLanguage
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException


class AiConversationShopRepositoryImpl(
    private val api: ShopApi,
    private val sharedPreferences: SharedPreferences,
) : AiConversationShopRepository {

    private val _aiTokensState = MutableStateFlow(
        AiTokensState(
            messagesCount = sharedPreferences.getInt(KEY_CACHED_MESSAGE_COUNT, 0),
            freeTrialAvailable = sharedPreferences.getBoolean(KEY_IS_TRIAL_AVAILABLE, true),
        )
    )

    private fun reloadAiTokensState() {
        _aiTokensState.value = AiTokensState(
            messagesCount = sharedPreferences.getInt(KEY_CACHED_MESSAGE_COUNT, 0),
            freeTrialAvailable = sharedPreferences.getBoolean(KEY_IS_TRIAL_AVAILABLE, true),
        )
    }

    override suspend fun tryMessagePack(userId: String, userToken: String): Result<Unit> {
        return try {

            val apiResponse = api.tryMessagesPack(
                userId = userId,
                token = userToken,
                appVersion = BuildConfig.VERSION_CODE,
            )

            if (apiResponse.isSuccessful) {
                val newTokensCount = apiResponse.body()?.newTokensCount
                if (newTokensCount == null) {
                    Result.failure(NoResponseException())
                } else {
                    saveUserMessageCount(newTokensCount)
                    setTrialUnavailability()
                    _aiTokensState.value = _aiTokensState.value.copy(
                        messagesCount = newTokensCount,
                        freeTrialAvailable = false,
                    )
                    Result.success(Unit)
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                Result.failure(exception)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buyMessagePack(
        userId: String,
        userToken: String,
        packId: String,
        orderId: String,
        purchaseToken: String,
        productId: String,
    ): Result<AiTokensState> {
        return try {
            val apiResponse = api.buyTokens(
                userId = userId,
                userToken = userToken,
                orderId = orderId,
                purchaseToken = purchaseToken,
                productId = productId,
                appVersion = BuildConfig.VERSION_CODE,
            )

            if (apiResponse.isSuccessful) {
                val response = apiResponse.body()
                if (response == null) {
                    Result.failure(NoResponseException())
                } else {
                    val domain = response.toDomain()
                    saveUserMessageCount(domain.messagesCount)
                    _aiTokensState.value = domain
                    Result.success(domain)
                }
            } else {
                Result.failure(ApiFailureResponseHandler.handler(apiResponse.errorBody()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun setTrialUnavailability() {
        sharedPreferences.edit {
            putBoolean(KEY_IS_TRIAL_AVAILABLE, false)
        }
    }

    override suspend fun isTrialAvailable(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_TRIAL_AVAILABLE, true)
    }

    override fun saveUserMessageCount(count: Int) {
        sharedPreferences.edit {
            putInt(KEY_CACHED_MESSAGE_COUNT, count)
        }
        _aiTokensState.value = _aiTokensState.value.copy(messagesCount = count)
    }

    override fun observeAiTokenState(): StateFlow<AiTokensState> {
        return _aiTokensState.asStateFlow()
    }

    override fun getAiTokenState(userId: String): Flow<Result<AiTokensState>> = flow {
        reloadAiTokensState()
        emit(Result.success(_aiTokensState.value))

        try {
            val response = api.getUserMessageCount(
                userId = userId,
                appVersion = BuildConfig.VERSION_CODE,
                timeZoneId = TimeZone.getDefault().id
            )

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val domain = dto.toDomain()
                    saveUserMessageCount(domain.messagesCount)
                    _aiTokensState.value = domain
                    emit(Result.success(domain))
                } ?: emit(Result.failure(NoResponseException("Empty tokens state response")))
            } else {
                val exception = ApiFailureResponseHandler.handler(response.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getAiMessagesPacks(): Result<List<AiMessagePack>> {
        val language = GameLanguage.determineLangDirectory()
        return try {
            val apiResponse = api.getAiMessagesPacks(language)
            if (apiResponse.isSuccessful) {
                val packs = apiResponse.body()?.data?.packs
                if (packs != null) {
                    val messagePacks = packs.map { it.toDomain() }.sortedBy { it.tokensCount }
                    Result.success(messagePacks)
                } else {
                    Result.failure(NoResponseException("Empty AI messages packs response"))
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                Result.failure(exception)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val KEY_IS_TRIAL_AVAILABLE = "IS_TRIAL_AVAILABLE"
        const val KEY_CACHED_MESSAGE_COUNT = "CACHED_MESSAGE_COUNT_KEY"
    }
}
