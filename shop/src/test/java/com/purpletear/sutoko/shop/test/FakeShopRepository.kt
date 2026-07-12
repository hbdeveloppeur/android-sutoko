package com.purpletear.sutoko.shop.test

import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeShopRepository : ShopRepository {

    private val balance = MutableStateFlow(Balance(coins = -1, diamonds = -1))

    override fun observeBalance(): Flow<Balance> = balance.asStateFlow()

    override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> {
        balance.value = Balance(coins = 100, diamonds = 0)
        return kotlinx.coroutines.flow.flowOf(Result.success(Unit))
    }

    override fun resetBalance() {
        balance.value = Balance(coins = -1, diamonds = -1)
    }

    override fun updateBalance(newBalance: Balance) {
        balance.value = newBalance
    }

    override suspend fun getPacks(): Result<List<ShopPack>> = Result.success(emptyList())
}
