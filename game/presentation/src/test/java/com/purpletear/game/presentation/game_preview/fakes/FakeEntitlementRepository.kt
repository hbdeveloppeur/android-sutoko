package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class FakeEntitlementRepository : EntitlementRepository {
    val isGrantedFlow = MutableStateFlow(false)
    val grantedSkusFlow = MutableStateFlow<Set<String>>(emptySet())
    val hasPremiumFlow = MutableStateFlow(false)

    var refreshGrantResult: Result<Boolean> = Result.success(false)
    var refreshGrantCalls = 0
        private set

    private val queuedResults = ArrayDeque<Result<Boolean>>()

    /** Results returned one per call, in order, taking priority over [refreshGrantResult]. */
    fun enqueueResults(resultsToQueue: List<Result<Boolean>>) {
        queuedResults.addAll(resultsToQueue)
    }

    // Per-sku granularity is not needed: the ViewModel only observes the
    // current catalog's SKUs. Premium also grants, like the real contract.
    override fun observeIsGranted(skuIdentifiers: List<String>): Flow<Boolean> =
        combine(isGrantedFlow, hasPremiumFlow) { granted, hasPremium -> granted || hasPremium }

    override fun observeGrantedSkus(): Flow<Set<String>> = grantedSkusFlow.asStateFlow()
    override fun observeHasPremium(): Flow<Boolean> = hasPremiumFlow.asStateFlow()

    override suspend fun refreshGrant(skuIdentifiers: List<String>): Result<Boolean> {
        refreshGrantCalls++
        val result = queuedResults.removeFirstOrNull() ?: refreshGrantResult
        // Mirrors the real implementation: a definitive grant updates the coin
        // cache, so observeIsGranted re-emits true.
        result.onSuccess { granted -> if (granted) isGrantedFlow.value = true }
        return result
    }
}
