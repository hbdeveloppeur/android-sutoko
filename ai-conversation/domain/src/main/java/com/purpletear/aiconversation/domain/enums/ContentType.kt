package com.purpletear.aiconversation.domain.enums

enum class ContentType(val code: String) {
    ImageGenerationRequest(code = "image_generation_request"),
    CharacterMessage(code = "new_character_message"),
}