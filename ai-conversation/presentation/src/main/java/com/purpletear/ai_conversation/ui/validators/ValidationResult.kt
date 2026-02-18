package com.purpletear.ai_conversation.ui.validators

import androidx.annotation.StringRes
import com.purpletear.ai_conversation.presentation.R

sealed class ValidationResult(@StringRes val message: Int? = null) {
    data object Success : ValidationResult()
    data class TooShort(val minLength: Int) :
        ValidationResult(R.string.ai_conversation_the_first_name_is_too_short)
}