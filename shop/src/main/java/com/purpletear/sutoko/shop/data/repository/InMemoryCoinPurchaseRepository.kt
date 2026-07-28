package com.purpletear.sutoko.shop.data.repository

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.data.remote.BuyCatalogProductRequestDto
import com.purpletear.sutoko.shop.data.remote.ShopApi
import com.purpletear.sutoko.shop.data.remote.ShopErrorResponseDto
import com.purpletear.sutoko.shop.data.remote.UserHasProductRequestDto
import com.purpletear.sutoko.shop.data.remote.toDomainModel
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryCoinPurchaseRepository @Inject constructor(
    private val api: ShopApi,
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository,
) : CoinPurchaseRepository {

    private data class CachedGrants(val userId: String, val skus: Set<String>)

    private val gson = Gson()
    private val cache = MutableStateFlow<CachedGrants?>(null)

    override fun observeCoinPurchasedSkus(): Flow<Set<String>> =
        combine(cache, userRepository.observeUser()) { cached, user ->
            if (cached != null && user != null && cached.userId == user.id) {
                cached.skus
            } else {
                emptySet()
            }
        }.distinctUntilChanged()

    override suspend fun buyStoryWithCoins(
        sku: String,
        userId: String,
    ): Result<Balance> {
        if (sku.isBlank()) {
            return Result.failure(IllegalArgumentException("sku must not be blank"))
        }

        return try {
            val response = api.buyCatalogProduct(
                BuyCatalogProductRequestDto(
                    skuIdentifier = sku,
                    userId = userId,
                    type = "story"
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(BuyStoryError.Unknown("Response body is null"))
                val balance = body.balance.toDomainModel()
                shopRepository.updateBalance(balance)
                addCachedSku(userId, sku)
                Result.success(balance)
            } else {
                val error = parseError(response.code(), response.errorBody()?.string())
                if (error is BuyStoryError.AlreadyOwned) {
                    addCachedSku(userId, sku)
                }
                Result.failure(error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(BuyStoryError.Network(e))
        } catch (e: Exception) {
            Result.failure(BuyStoryError.Unknown(e.message))
        }
    }

    override suspend fun isStoryGranted(
        userId: String,
        skuIdentifiers: List<String>,
    ): Result<Boolean> {
        if (skuIdentifiers.isEmpty()) {
            return Result.success(false)
        }

        val cached = cache.value?.takeIf { it.userId == userId }?.skus ?: emptySet()
        if (skuIdentifiers.any { it in cached }) {
            return Result.success(true)
        }

        return try {
            val response = api.userHasProduct(
                UserHasProductRequestDto(
                    userId = userId,
                    skuIdentifiers = skuIdentifiers
                )
            )

            if (response.isSuccessful) {
                val granted = response.body()?.granted == true
                if (granted) {
                    cache.value = CachedGrants(
                        userId,
                        (cache.value?.takeIf { it.userId == userId }?.skus ?: emptySet()) + skuIdentifiers
                    )
                }
                Result.success(granted)
            } else {
                Result.failure(BuyStoryError.Unknown(response.errorBody()?.string()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(BuyStoryError.Network(e))
        } catch (e: Exception) {
            Result.failure(BuyStoryError.Unknown(e.message))
        }
    }

    @VisibleForTesting
    internal fun addCachedSku(userId: String, sku: String) {
        cache.value = CachedGrants(
            userId,
            (cache.value?.takeIf { it.userId == userId }?.skus ?: emptySet()) + sku
        )
    }

    private fun parseError(code: Int, errorBody: String?): BuyStoryError {
        val errorCode = runCatching {
            gson.fromJson(errorBody, ShopErrorResponseDto::class.java)?.code
        }.getOrNull()

        return when (errorCode) {
            "ItemAlreadyOwnedError" -> BuyStoryError.AlreadyOwned()
            "ValidationError" -> BuyStoryError.NotPurchasable()
            else -> BuyStoryError.Unknown("HTTP $code: $errorBody")
        }
    }
}
