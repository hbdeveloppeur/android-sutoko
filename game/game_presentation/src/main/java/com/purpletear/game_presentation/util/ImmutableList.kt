package com.purpletear.game_presentation.util

import androidx.annotation.Keep

/**
 * Immutable wrapper for List to be used in Compose
 */
@Keep
data class ImmutableList<T>(val items: List<T>)

/**
 * Immutable wrapper for Map to be used in Compose
 */
@Keep
data class ImmutableMap<T, Y>(val map: Map<T, Y>)
