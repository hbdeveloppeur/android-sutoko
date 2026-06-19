package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.shop.data.remote.GetBalanceRequestDto
import com.purpletear.sutoko.shop.data.remote.ShopApi
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryShopRepository @Inject constructor(
    private val api: ShopApi,
) : ShopRepository {

    private val _balance = MutableStateFlow(Balance(coins = -1, diamonds = -1))

    override fun observeBalance(): Flow<Balance> = _balance.asStateFlow()

    override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> = flow {
        try {
            val request = GetBalanceRequestDto(
                userId = userId,
                userToken = userToken
            )
            val response = api.getBalance(request)

            if (response.isSuccessful) {
                val balanceResponse = response.body()
                if (balanceResponse != null) {
                    _balance.value = balanceResponse.toDomainModel()
                    emit(Result.success(Unit))
                } else {
                    emit(Result.failure(Exception("Response body is null")))
                }
            } else {
                val errorMessage = response.errorBody()?.string()
                emit(Result.failure(Exception("HTTP ${response.code()}: $errorMessage")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            emit(Result.failure(Exception("Network error", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getPacks(): Result<List<ShopPack>> {
        val list = listOf<ShopPack>(
            ShopPack(
                coins = 550,
                diamonds = 550,
                sku = "coins_pack_starter",
                type = CoinsPackType.Low
            ),
            ShopPack(
                coins = 1500,
                diamonds = 1500,
                sku = "coins_pack_treasure",
                type = CoinsPackType.Medium
            ),
            ShopPack(
                coins = 3000,
                diamonds = 3000,
                sku = "coins_pack_mega",
                type = CoinsPackType.High
            ),
            ShopPack(
                coins = 1000,
                diamonds = 1000,
                sku = "sutoko_premium_yearly_69",
                type = CoinsPackType.Premium
            ),
        )
        return Result.success(list)
    }
}
