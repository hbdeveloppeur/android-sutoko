package com.purpletear.aiconversation.domain.enums

sealed class Direction {
    data object Forward : Direction()
    data object Backward : Direction()
}
