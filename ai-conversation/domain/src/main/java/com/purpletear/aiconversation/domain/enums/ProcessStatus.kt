package com.purpletear.aiconversation.domain.enums

import androidx.annotation.Keep

@Keep
enum class ProcessStatus(val code: String) {
    INITIAL("INITIAL"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");
}