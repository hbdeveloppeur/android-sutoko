package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.usecase.UserNickNameSanitizer

/**
 * A dialog that asks the user for a nickname.
 *
 * The input is validated against [UserNickNameSanitizer]; if invalid the dialog
 * stays open and shows an inline error.
 *
 * @param onConfirm Called with the sanitized nickname when the user presses confirm.
 * @param onDismiss Called when the user cancels or dismisses the dialog.
 */
@Composable
fun NickNameInputDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val name = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.first_name_prompt_title)) },
        text = {
            TextField(
                value = name.value,
                onValueChange = {
                    name.value = it
                    showError.value = false
                },
                placeholder = { Text(stringResource(R.string.first_name_prompt_placeholder)) },
                singleLine = true,
                isError = showError.value,
                supportingText = if (showError.value) {
                    { Text(stringResource(R.string.first_name_prompt_error)) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sanitized = UserNickNameSanitizer.sanitize(name.value)
                    if (sanitized != null) {
                        onConfirm(sanitized)
                    } else {
                        showError.value = true
                    }
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
