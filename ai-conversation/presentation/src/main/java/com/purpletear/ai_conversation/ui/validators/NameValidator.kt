package com.purpletear.ai_conversation.ui.validators

object NameValidator {
    fun validate(name: String): ValidationResult {
        return if (name.replace(" ", "").length >= 3) {
            ValidationResult.Success
        } else {
            ValidationResult.TooShort(minLength = 3)
        }
    }
}