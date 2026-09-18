package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.usecase.BuyStoryWithCoinsUseCase

class FakeBuyStoryWithCoinsUseCase : BuyStoryWithCoinsUseCase(
    coinPurchaseRepository = FakeCoinPurchaseRepository(),
    userRepository = FakeUserRepository(),
) {
    var beforePurchase: suspend () -> Unit = {}
    val calls = mutableListOf<String>()

    private val results = mutableMapOf<String, Result<Balance>>()

    fun setResult(sku: String, result: Result<Balance>) {
        results[sku] = result
    }

    override suspend fun invoke(sku: String): Result<Balance> {
        calls += sku
        beforePurchase()
        return results[sku] ?: Result.success(Balance(coins = 0, diamonds = 0))
    }
}
