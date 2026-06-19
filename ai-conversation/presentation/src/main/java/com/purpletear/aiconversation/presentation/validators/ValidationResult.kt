package com.purpletear.aiconversation.presentation.validators

import androidx.annotation.StringRes
import com.purpletear.aiconversation.presentation.R
import androidx.annotation.Keep

sealed class ValidationResult(@StringRes val message: Int? = null) {
    data object Success : ValidationResult()
    @Keep
    data class TooShort(val minLength: Int) :
        ValidationResult(R.string.ai_conversation_the_first_name_is_too_short)
}