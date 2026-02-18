package com.purpletear.aiconversation.presentation.component.input.text

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purpletear.aiconversation.presentation.validators.ValidationResult

internal open class InputTextViewModel : ViewModel() {

    private var _text: MutableState<String> = mutableStateOf("")
    val text: State<String>
        get() = _text

    private var _error: MutableState<ValidationResult> = mutableStateOf(ValidationResult.Success)
    val validationResult: State<ValidationResult>
        get() = _error

    fun onValueChange(text: String) {
        _text.value = text
        _error.value = ValidationResult.Success
    }

    fun handle(validationResult: ValidationResult) {
        _error.value = validationResult
    }
}