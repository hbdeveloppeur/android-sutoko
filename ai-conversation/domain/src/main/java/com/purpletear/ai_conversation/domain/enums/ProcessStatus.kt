package com.purpletear.ai_conversation.domain.enums

enum class ProcessStatus(val code: String) {
    INITIAL("INITIAL"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");
}