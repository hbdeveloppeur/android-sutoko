package com.purpletear.aiconversation.domain.enums

enum class ProcessStatus(val code: String) {
    INITIAL("INITIAL"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");
}