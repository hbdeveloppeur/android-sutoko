package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.usecase.ObserveCoinPurchasedSkusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeObserveCoinPurchasedSkusUseCase : ObserveCoinPurchasedSkusUseCase(
    coinPurchaseRepository = FakeCoinPurchaseRepository()
) {
    private val skus = MutableStateFlow<Set<String>>(emptySet())

    fun setSkus(value: Set<String>) {
        skus.value = value
    }

    override fun invoke(): Flow<Set<String>> = skus.asStateFlow()
}
