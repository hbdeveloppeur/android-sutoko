package com.purpletear.shop.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.shop.data.dto.BuyCatalogProductRequestDto
import com.purpletear.shop.data.dto.GetBalanceRequestDto
import com.purpletear.shop.data.dto.RegisterOrderRequestDto
import com.purpletear.shop.data.dto.UserHasProductRequestDto
import com.purpletear.shop.data.dto.toDomainModel
import com.purpletear.shop.data.exception.InternetConnectivityException
import com.purpletear.shop.data.remote.CatalogApi
import com.purpletear.shop.data.utils.ApiFailureResponseHandler
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementation of the CatalogRepository interface.
 */
class CatalogRepositoryImpl(
    private val api: CatalogApi,
    private val sharedPreferences: SharedPreferences,
) : CatalogRepository {
    private var _shopBalance: MutableStateFlow<Balance?> = MutableStateFlow(null)
    override val shopBalance: StateFlow<Balance?> = _shopBalance


    private val AcknowledgedOrdersKeys = "AcknowledgedOrdersKeys"
    private val gson = Gson()

    /**
     * Buy a catalog product
     *
     * @param userId The ID of the user making the purchase
     * @param skuIdentifier The identifier of the product to buy
     * @param type The type of the product
     * @return A Flow containing the Result of the purchase operation
     */
    override suspend fun buyCatalogProduct(
        userId: String,
        skuIdentifier: String,
        type: String,
    ): Flow<Result<Unit>> = flow {
        val crashlytics = FirebaseCrashlytics.getInstance().apply {
            setCustomKey("buyCatalogProduct.userId", userId)
            setCustomKey("buyCatalogProduct.skuIdentifier", skuIdentifier)
            setCustomKey("buyCatalogProduct.type", type)
            setCustomKey("buyCatalogProduct.startedAt", System.currentTimeMillis())
            setCustomKey("buyCatalogProduct.thread", Thread.currentThread().name)
            log("buyCatalogProduct → Purchase flow started")
        }

        try {
            val request = BuyCatalogProductRequestDto(
                skuIdentifier = skuIdentifier,
                userId = userId,
                type = type
            )
            val response = api.buyCatalogProduct(request)

            crashlytics.setCustomKey("buyCatalogProduct.httpStatus", response.code())
            crashlytics.setCustomKey("buyCatalogProduct.isSuccessful", response.isSuccessful)

            if (response.isSuccessful) {
                crashlytics.log("buyCatalogProduct → Purchase completed successfully")
                emit(Result.success(Unit))
            } else {
                crashlytics.log("buyCatalogProduct → API responded with failure")
                crashlytics.setCustomKey(
                    "buyCatalogProduct.errorBodyPresent",
                    response.errorBody() != null
                )

                val exception = ApiFailureResponseHandler.handler(response.errorBody())
                crashlytics.recordException(exception)

                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.apply {
                log("buyCatalogProduct → HttpException (${e.code()})")
                setCustomKey("buyCatalogProduct.httpExceptionCode", e.code())
                setCustomKey(
                    "buyCatalogProduct.httpExceptionUrl",
                    e.response()?.raw()?.request?.url?.toString() ?: "unknown"
                )
                recordException(e)
            }
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("buyCatalogProduct → IOException (likely connectivity)")
                setCustomKey("buyCatalogProduct.connectivityIssue", true)
                recordException(e)
            }
            emit(
                Result.failure(
                    InternetConnectivityException(
                        "Failed to connect to the server due to internet issues",
                        e
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("buyCatalogProduct → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }

    /**
     * Check if a user has specific products
     *
     * @param userId The ID of the user to check
     * @param skuIdentifiers List of product identifiers to check
     * @return A Flow containing the Result of the check operation with a boolean indicating if the user has the products
     */
    override suspend fun userHasProduct(
        userId: String,
        skuIdentifiers: List<String>,
    ): Flow<Result<Boolean>> = flow {
        val crashlytics = FirebaseCrashlytics.getInstance().also { crash ->
            crash.setCustomKey("feature", "userHasProduct")
            crash.setCustomKey("userHasProduct_user_hash", userId.hashCode())
            crash.setCustomKey("userHasProduct_sku_count", skuIdentifiers.size)
            crash.setCustomKey(
                "userHasProduct_skus_preview",
                skuIdentifiers.take(5).joinToString(",").ifEmpty { "none" }
            )
        }

        try {
            val request = UserHasProductRequestDto(
                userId = userId,
                skuIdentifiers = skuIdentifiers
            )
            val response = api.userHasProduct(request)
            if (response.isSuccessful) {
                val result = response.body()?.granted ?: false
                emit(Result.success(result))
            } else {
                crashlytics.log("userHasProduct → API responded with failure")
                crashlytics.setCustomKey(
                    "userHasProduct.errorBodyPresent",
                    response.errorBody() != null
                )
                val exception = ApiFailureResponseHandler.handler(response.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.recordException(e)
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("userHasProduct → IOException (likely connectivity)")
                setCustomKey("userHasProduct.connectivityIssue", true)
                recordException(e)
            }
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("userHasProduct → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }

    /**
     * Register an order with the catalog service
     *
     * @param purchaseToken The purchase token from the payment provider
     * @param skuIdentifier SKU identifier for the products in the order
     * @param userId The ID of the user making the purchase
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the registration operation
     */
    override suspend fun registerOrder(
        purchaseToken: String,
        skuIdentifier: String,
        userId: String,
        userToken: String
    ): Flow<Result<Unit>> = flow {
        val crashlytics = FirebaseCrashlytics.getInstance().also { crash ->
            crash.setCustomKey("feature", "registerOrder")
            crash.setCustomKey("registerOrder_user_hash", userId.hashCode())
            crash.setCustomKey("registerOrder_sku", skuIdentifier)
            crash.setCustomKey("registerOrder_purchaseToken_hash", purchaseToken.hashCode())
        }

        val acknowledgedProducts = isAcknowledgedOrders(arrayOf(purchaseToken))
        if (acknowledgedProducts.isNotEmpty()) {
            crashlytics.log("registerOrder → already acknowledged purchaseToken")
            crashlytics.setCustomKey("registerOrder_preAcknowledged", true)
            emit(Result.success(Unit))
            return@flow
        }
        try {
            val request = RegisterOrderRequestDto(
                purchaseToken = purchaseToken,
                skuIdentifier = skuIdentifier,
                userId = userId,
                userToken = userToken
            )
            val response = api.registerOrder(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    _shopBalance.value = it.toDomainModel()
                }
                // Mark this purchaseToken as acknowledged
                addToAcknowledgedOrders(arrayOf(purchaseToken))
                emit(Result.success(Unit))
            } else {
                crashlytics.log("registerOrder → API responded with failure")
                crashlytics.setCustomKey(
                    "registerOrder.errorBodyPresent",
                    response.errorBody() != null
                )
                val exception = ApiFailureResponseHandler.handler(response.errorBody())
                crashlytics.recordException(exception)
                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.recordException(e)
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("registerOrder → IOException (likely connectivity)")
                setCustomKey("registerOrder.connectivityIssue", true)
                recordException(e)
            }
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("registerOrder → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }

    /**
     * Adds the provided purchase tokens to the list of acknowledged orders.
     *
     * @param purchaseTokens An array of purchase tokens to be added to the acknowledged orders.
     */
    override fun addToAcknowledgedOrders(purchaseTokens: Array<String>) {
        val acknowledgedProducts = getAcknowledgedOrders().toMutableSet()
        acknowledgedProducts.addAll(purchaseTokens)

        val json = gson.toJson(acknowledgedProducts)
        sharedPreferences.edit { putString(AcknowledgedOrdersKeys, json) }
    }

    /**
     * Checks whether the provided purchase tokens are part of the acknowledged products.
     *
     * @param purchaseTokens An array of purchase tokens to check.
     * @return A map where each key is a purchase token from the input array, and the corresponding value is a boolean
     * indicating whether that token is acknowledged (true) or not (false).
     */
    override fun isAcknowledgedOrders(purchaseTokens: Array<String>): Map<String, Boolean> {
        val acknowledgedProducts = getAcknowledgedOrders()
        val result = mutableMapOf<String, Boolean>()

        acknowledgedProducts.forEach { token ->
            if (token in purchaseTokens) {
                result[token] = true
            }
        }

        return result
    }

    /**
     * Get the set of acknowledged products from SharedPreferences
     *
     * @return Set of acknowledged product SKU identifiers
     */
    private fun getAcknowledgedOrders(): Set<String> {
        val json = sharedPreferences.getString(AcknowledgedOrdersKeys, null)

        return if (json != null) {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptySet()
        }
    }

    /**
     * Get the user's balance of coins and diamonds
     *
     * @param userId The ID of the user
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the balance retrieval operation
     */
    override suspend fun getBalance(
        userId: String,
        userToken: String
    ): Flow<Result<Balance>> = flow {
        val crashlytics = FirebaseCrashlytics.getInstance().also { crash ->
            crash.setCustomKey("feature", "getBalance")
            crash.setCustomKey("getBalance_user_hash", userId)
        }
        try {
            val request = GetBalanceRequestDto(
                userId = userId,
                userToken = userToken
            )
            val response = api.getBalance(request)
            if (response.isSuccessful) {
                val balanceResponse = response.body()
                if (balanceResponse != null) {
                    val balance = balanceResponse.toDomainModel()
                    _shopBalance.value = balance
                    emit(Result.success(balance))
                } else {
                    crashlytics.log("getBalance → Response body is null")
                    val exception = Exception("Response body is null")
                    crashlytics.recordException(exception)
                    emit(Result.failure(exception))
                }
            } else {
                crashlytics.log("getBalance → API responded with failure")
                crashlytics.setCustomKey(
                    "getBalance.errorBodyPresent",
                    response.errorBody() != null
                )
                val exception = ApiFailureResponseHandler.handler(response.errorBody())
                crashlytics.recordException(exception)
                emit(Result.failure(exception))
            }
        } catch (e: HttpException) {
            crashlytics.recordException(e)
            emit(Result.failure(e))
        } catch (e: IOException) {
            crashlytics.apply {
                log("getBalance → IOException (likely connectivity)")
                setCustomKey("getBalance.connectivityIssue", true)
                recordException(e)
            }
            emit(
                Result.failure(
                    InternetConnectivityException(
                        "Failed to connect to the server due to internet issues",
                        e
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        if (e !is CancellationException) {
            FirebaseCrashlytics.getInstance().apply {
                log("getBalance → Flow catch: ${e.javaClass.simpleName}")
                recordException(e)
            }
        }
        emit(Result.failure(e))
    }
}
