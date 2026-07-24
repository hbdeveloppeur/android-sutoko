package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.usecase.IsStoryGrantedUseCase

class FakeIsStoryGrantedUseCase : IsStoryGrantedUseCase(
    coinPurchaseRepository = FakeCoinPurchaseRepository(),
    userRepository = FakeUserRepository(),
) {
    private val results = mutableMapOf<List<String>, Result<Boolean>>()
    private val queuedResults = ArrayDeque<Result<Boolean>>()

    var calls = 0
        private set

    fun setResult(skuIdentifiers: List<String>, result: Result<Boolean>) {
        results[skuIdentifiers] = result
    }

    /** Results returned one per call, in order, taking priority over [setResult]. */
    fun enqueueResults(resultsToQueue: List<Result<Boolean>>) {
        queuedResults.addAll(resultsToQueue)
    }

    override suspend fun invoke(skuIdentifiers: List<String>): Result<Boolean> {
        calls++
        return queuedResults.removeFirstOrNull()
            ?: results[skuIdentifiers]
            ?: Result.success(false)
    }
}
