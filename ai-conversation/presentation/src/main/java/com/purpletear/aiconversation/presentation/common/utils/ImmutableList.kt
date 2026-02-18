package com.purpletear.aiconversation.presentation.common.utils

import androidx.compose.runtime.Immutable

@Immutable
data class ImmutableList<T>(val items: List<T>)

@Immutable
data class ImmutableMap<T, Y>(val map: Map<T, Y>)