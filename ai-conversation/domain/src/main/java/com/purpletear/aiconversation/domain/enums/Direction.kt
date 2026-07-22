package com.purpletear.aiconversation.domain.enums

import androidx.annotation.Keep

@Keep
sealed class Direction {
    data object Forward : Direction()
    data object Backward : Direction()
}
