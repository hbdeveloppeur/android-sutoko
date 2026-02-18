package fr.purpletear.sutoko.presentation.util

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable


@Immutable
@Keep
data class ImmutableList<T>(val items: List<T>)

@Immutable
@Keep
data class ImmutableMap<T, Y>(val map: Map<T, Y>)
