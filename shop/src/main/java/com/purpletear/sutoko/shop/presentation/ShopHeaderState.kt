package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.shop.domain.repository.model.Balance

/**
 * UI state of the shop header (coins/diamonds area).
 *
 * - [Disconnected]: no authenticated user — show the sign-in button instead of a balance.
 * - [Loading]: user connected, balance not yet fetched — show the loading indicator.
 * - [Loaded]: balance available — show coins and diamonds.
 */
sealed interface ShopHeaderState {
    data object Disconnected : ShopHeaderState
    data object Loading : ShopHeaderState
    data class Loaded(val balance: Balance) : ShopHeaderState
}
