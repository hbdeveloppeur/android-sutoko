package com.purpletear.shop.data.repository

import android.content.SharedPreferences
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.shop.data.dto.toDomain
import com.purpletear.shop.data.exception.ShopApiException
import com.purpletear.shop.data.remote.ShopApi
import com.purpletear.shop.data.utils.ApiFailureResponseHandler
import com.purpletear.shop.domain.model.AiCustomerState
import com.purpletear.shop.domain.model.AiMessagePack
import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import purpletear.fr.purpleteartools.GameLanguage
import retrofit2.HttpException
import java.io.IOException
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException

class ShopRepositoryImpl(
    private val api: ShopApi,
    private val sharedPreferences: SharedPreferences,
) : ShopRepository {
    private val cachedMessageCountKey = "CACHED_MESSAGE_COUNT_KEY"
    private val isTrialAvailable = "IS_TRIAL_AVAILABLE"
    private val trialMessageCount: Int = 10
    private var _isOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isOpen: StateFlow<Boolean> get() = _isOpen

    private fun getTimeZoneCode(): String {
        val timeZone = TimeZone.getDefault()
        return timeZone.id
    }

    override suspend fun getUserAccountState(userId: String): Flow<Result<AiCustomerState>> = flow {
        try {
            val cachedCount = sharedPreferences.getInt(cachedMessageCountKey, -1)
            if (cachedCount != -1) {
                emit(
                    Result.success(
                        AiCustomerState(
                            messagesCount = cachedCount,
                            freeTrialAvailable = false,
                            canWatchAd = false
                        )
                    )
                )
            }

            val response = api.getUserMessageCount(
                userId,
                1,
                getTimeZoneCode()
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    val count = it.count
                    sharedPreferences.edit().putInt(cachedMessageCountKey, count).apply()
                    emit(Result.success(it.toDomain()))
                } ?: emit(Result.failure(Exception("Response body is null")))
            } else {
                val exception: ShopApiException =
                    ApiFailureResponseHandler.handler(response.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }


    override fun openDialog() {
        _isOpen.value = true
    }

    override fun closeDialog() {
        _isOpen.value = false
    }

    override suspend fun getAiMessagesPacks(): Flow<Result<List<AiMessagePack>>> = flow {
        val language = GameLanguage.determineLangDirectory()
        val crashlytics = FirebaseCrashlytics.getInstance().also { crash ->
            crash.setCustomKey("getAiMessagesPacks_language", language)
            crash.log("getAiMessagesPacks → Flow started")
        }
        try {
            val apiResponse = api.getAiMessagesPacks(language)

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { response ->
                    delay(280L)
                    val characters =
                        response.data.packs.map { it.toDomain() }.sortedBy { it.tokensCount }
                    emit(Result.success(characters))
                } ?: run {
                    crashlytics.log("getAiMessagesPacks → Response body is null")
                    val exception = ShopApiException()
                    crashlytics.recordException(exception)
                    emit(Result.failure(exception))
                    return@flow
                }
            } else {
                crashlytics.log("getAiMessagesPacks → API responded with failure")
                crashlytics.setCustomKey(
                    "getAiMessagesPacks.errorBodyPresent",
                    apiResponse.errorBody() != null
                )
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                crashlytics.recordException(exception)
                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.recordException(e)
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("getAiMessagesPacks → IOException (likely connectivity)")
                setCustomKey("getAiMessagesPacks.connectivityIssue", true)
                recordException(e)
            }
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("getAiMessagesPacks → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }

    override suspend fun buy(
        userId: String,
        userToken: String,
        orderId: String,
        purchaseToken: String,
        productId: String,
    ): Flow<Result<AiCustomerState>> = flow {
        val crashlytics = FirebaseCrashlytics.getInstance().also { crash ->
            crash.setCustomKey("buy_user_hash", userId.hashCode())
            crash.setCustomKey("buy_userToken_hash", userToken.hashCode())
            crash.setCustomKey("buy_orderId_hash", orderId.hashCode())
            crash.setCustomKey("buy_purchaseToken_hash", purchaseToken.hashCode())
            crash.setCustomKey("buy_productId", productId)
            crash.log("buy → Flow started")
        }
        try {
            val apiResponse = api.buyTokens(
                userId = userId,
                userToken = userToken,
                orderId = orderId,
                purchaseToken = purchaseToken,
                productId = productId,
                appVersion = 1,
            )

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { response ->
                    delay(280L)
                    emit(Result.success(response.toDomain()))
                } ?: run {
                    crashlytics.log("buy → Response body is null")
                    val exception = ShopApiException()
                    crashlytics.recordException(exception)
                    emit(Result.failure(exception))
                    return@flow
                }
            } else {
                crashlytics.log("buy → API responded with failure")
                crashlytics.setCustomKey(
                    "buy.errorBodyPresent",
                    apiResponse.errorBody() != null
                )
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                crashlytics.recordException(exception)
                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.recordException(e)
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("buy → IOException (likely connectivity)")
                setCustomKey("buy.connectivityIssue", true)
                recordException(e)
            }
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("buy → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }

    override suspend fun preBuy(
        orderId: String,
        purchaseToken: String,
        productId: String
    ): Flow<Result<Unit>> = flow {
        // TODO
        val apiResponse = api.preBuy(
            orderId = orderId,
            purchaseToken = purchaseToken,
            productId = productId,
            appVersion = 1,
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let {
                delay(280L)
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(ShopApiException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun tryMessagePack(uid: String, userToken: String): Flow<Result<Boolean>> =
        flow {
            // TODO
            val apiResponse = api.tryMessagesPack(
                userId = uid,
                token = userToken,
                appVersion = 1,
            )

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { response ->
                    delay(280L)
                    saveUserMessageCount(trialMessageCount)
                    saveUserState(trialMessageCount, false)
                    Result.success(true)
                    emit(Result.success(true))
                } ?: run {
                    emit(Result.failure(ShopApiException()))
                    return@flow
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                emit(Result.failure(exception))
            }
        }.catch {
            emit(Result.failure(it))
        }


    private fun saveUserMessageCount(count: Int) {
        try {
            sharedPreferences.edit().putInt(cachedMessageCountKey, count).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveUserState(count: Int, trialAvailable: Boolean) {
        sharedPreferences.edit().apply {
            putInt(cachedMessageCountKey, count)
            putBoolean(isTrialAvailable, trialAvailable)
            apply()
        }
    }

}