package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.shop.domain.repository.model.Balance

/**
 * UI state of the shop header (coins/diamonds area).
 *
 * - [Disconnected]: no authenticated user — show the sign-in button instead of a balance.
 * - [Loading]: user connected, balance not yet fetched — show the loading indicator.
 * - [Failed]: user connected but the balance load failed (e.g. offline) — show a
 *   retryable unavailable placeholder instead of meaningless sentinel values.
 *   The sign-in button is only shown for [Disconnected].
 * - [Loaded]: balance available — show coins and diamonds.
 */
sealed interface ShopHeaderState {
    data object Disconnected : ShopHeaderState
    data object Loading : ShopHeaderState
    data object Failed : ShopHeaderState
    data class Loaded(val balance: Balance) : ShopHeaderState
}
