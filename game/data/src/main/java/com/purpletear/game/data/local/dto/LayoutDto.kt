package com.purpletear.game.data.local.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Archive `layout.json` of a chapter, e.g. `{"sides":{"right":[7166]}}`.
 */
@Keep
data class LayoutDto(
    @SerializedName("sides") val sides: SidesDto? = null
) {
    @Keep
    data class SidesDto(
        @SerializedName("right") val right: List<Int>? = null
    )
}
