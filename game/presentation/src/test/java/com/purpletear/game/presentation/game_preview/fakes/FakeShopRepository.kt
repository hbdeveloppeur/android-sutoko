package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Starts unloaded (`Balance(-1, -1)`), mirroring the real repository before
 * the first successful load. Set [balanceFlow] to simulate a known balance.
 */
class FakeShopRepository : ShopRepository {

    val balanceFlow = MutableStateFlow(Balance(coins = -1, diamonds = -1))

    override fun observeBalance(): Flow<Balance> = balanceFlow.asStateFlow()

    override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> =
        flowOf(Result.success(Unit))

    override fun resetBalance() {
        balanceFlow.value = Balance(coins = -1, diamonds = -1)
    }

    override fun updateBalance(balance: Balance) {
        balanceFlow.value = balance
    }

    override suspend fun getPacks(): Result<List<ShopPack>> = Result.success(emptyList())
}
