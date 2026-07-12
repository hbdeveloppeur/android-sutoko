package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.usecase.IsStoryGrantedUseCase

class FakeIsStoryGrantedUseCase : IsStoryGrantedUseCase(
    coinPurchaseRepository = FakeCoinPurchaseRepository(),
    userRepository = FakeUserRepository(),
) {
    private val results = mutableMapOf<List<String>, Result<Boolean>>()

    fun setResult(skuIdentifiers: List<String>, result: Result<Boolean>) {
        results[skuIdentifiers] = result
    }

    override suspend fun invoke(skuIdentifiers: List<String>): Result<Boolean> {
        return results[skuIdentifiers] ?: Result.success(false)
    }
}
