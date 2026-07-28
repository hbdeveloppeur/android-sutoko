package com.purpletear.sutoko.shop.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for story entitlements.
 *
 * Contract: an entitlement is considered granted ONLY when server-confirmed:
 * a Play Billing purchase whose registration the backend accepted
 * (`backendRegistered == true`), or a coin purchase confirmed by the backend.
 * Unknown, ambiguous or offline state emits `false` (fail-closed): the UI must
 * never offer a Download action without a server-confirmed grant.
 */
interface EntitlementRepository {

    /**
     * Emits true only when at least one of [skuIdentifiers] has a
     * server-confirmed entitlement, or the user has a server-confirmed premium.
     */
    fun observeIsGranted(skuIdentifiers: List<String>): Flow<Boolean>

    /**
     * Emits the set of SKUs with a server-confirmed entitlement.
     */
    fun observeGrantedSkus(): Flow<Set<String>>

    /**
     * Emits true only for a server-confirmed premium purchase.
     */
    fun observeHasPremium(): Flow<Boolean>

    /**
     * Forces a server check for [skuIdentifiers].
     *
     * [Result.success] is only returned for a definitive server answer; a
     * missing/not-yet-loaded user is a [Result.failure] so callers can retry.
     */
    suspend fun refreshGrant(skuIdentifiers: List<String>): Result<Boolean>
}
