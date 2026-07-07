package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sharedelements.theme.Poppins
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.usecase.UserNickNameSanitizer

private val DialogBackground = Color(0xFF1B1D22)
private val InputBackground = Color(0x802A2D35)
private val PinkAccent = Color(0xFFFF007A)
private val SubtleText = Color(0xFFB0B0B0)

/**
 * A dialog that asks the user for a nickname.
 *
 * The input is validated against [UserNickNameSanitizer]; if invalid the dialog
 * stays open and shows an inline error.
 *
 * @param onConfirm Called with the sanitized nickname when the user presses confirm,
 *                  or `null` if the input is blank (the caller should use the default name).
 * @param onDismiss Called when the user cancels or dismisses the dialog.
 */
@Composable
fun NickNameInputDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val name = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(12.dp),
            color = DialogBackground,
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 4.dp)
                        .padding(horizontal = 12.dp),
                    text = stringResource(R.string.first_name_prompt_title),
                    color = Color.White,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                )

                TextField(
                    value = name.value,
                    onValueChange = {
                        name.value = it
                        showError.value = false
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.first_name_prompt_placeholder),
                            color = SubtleText,
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                        )
                    },
                    singleLine = true,
                    isError = showError.value,
                    supportingText = if (showError.value) {
                        {
                            Text(
                                text = stringResource(
                                    R.string.first_name_prompt_error,
                                    UserNickNameSanitizer.MIN_LENGTH,
                                    UserNickNameSanitizer.MAX_LENGTH,
                                ),
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        disabledContainerColor = InputBackground,
                        errorContainerColor = InputBackground,
                        errorTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = MaterialTheme.colorScheme.error,
                        cursorColor = PinkAccent,
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = SubtleText,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }

                    Button(
                        onClick = {
                            val trimmed = name.value.trim()
                            if (trimmed.isEmpty()) {
                                onConfirm(null)
                                return@Button
                            }

                            val sanitized = UserNickNameSanitizer.sanitize(trimmed)
                            if (sanitized != null) {
                                onConfirm(sanitized)
                            } else {
                                showError.value = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(android.R.string.ok),
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun NickNameInputDialogPreview() {
    SutokoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            NickNameInputDialog(
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
