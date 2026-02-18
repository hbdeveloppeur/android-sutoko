package com.purpletear.ai_conversation.domain.enums

sealed class Direction {
    data object Forward : Direction()
    data object Backward : Direction()
}
