package com.purpletear.aiconversation.domain.enums

import androidx.annotation.Keep

@Keep
enum class Gender(val code: String, val n: Int) {
    Male("male", 2),
    Female("female", 1),
}