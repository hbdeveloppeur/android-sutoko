package com.purpletear.game.presentation.game_preview.handlers

import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Encapsulates the purchase-flow state for the game preview screen.
 *
 * This handler intentionally covers only the action side of purchasing
 * (start/abort/confirm). Observation of already-purchased SKUs or premium
 * status remains the responsibility of the ViewModel, which assembles the
 * overall preview UI state.
 */
class GamePreviewPurchaseHandler @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
) {
    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _isPurchaseLoading = MutableStateFlow(false)
    val isPurchaseLoading: StateFlow<Boolean> = _isPurchaseLoading.asStateFlow()

    fun startPurchaseFlow() {
        _isPurchasing.value = true
    }

    fun abortPurchaseFlow() {
        reset()
    }

    suspend fun confirmPurchase(sku: String): Result<Unit> {
        _isPurchaseLoading.value = true
        return purchaseRepository.purchase(sku).also { reset() }
    }

    private fun reset() {
        _isPurchasing.value = false
        _isPurchaseLoading.value = false
    }
}
