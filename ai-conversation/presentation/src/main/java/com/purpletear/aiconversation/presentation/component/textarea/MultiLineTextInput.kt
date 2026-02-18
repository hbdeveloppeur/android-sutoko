package com.purpletear.aiconversation.presentation.component.textarea

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme

@Composable
@Preview(name = "TextAreaComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_description_textarea),
                contentDescription = null,
            )
            MultiLineTextInput(
                text = "",
                modifier = Modifier
                    .fillMaxWidth(0.92f),
                placeholder = "Description...",
                strokeColor = Color(0xFF537399),
            )
        }
    }
}

private fun findWordBoundaries(text: String, charIndex: Int): Pair<Int, Int> {
    var start = charIndex
    while (start > 0 && !text[start - 1].isWhitespace()) {
        start--
    }

    var end = charIndex
    while (end < text.length && !text[end].isWhitespace()) {
        end++
    }

    return start to end
}

@Composable
fun MultiLineTextInput(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    strokeColor: Color? = null,
    placeholder: String? = null,
    onChange: (text: String) -> Unit = {},
    onFocused: (Boolean) -> Unit = {},
    enabled: Boolean = true,
) {

    val hasFocus = remember { mutableStateOf(false) }
    val maxChar = 250
    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color(0xFF537399),
        backgroundColor = Color(0xFF81DEEA).copy(0.3f)
    )

    val strokeModifier = if (strokeColor != null) {
        Modifier.border(1.dp, strokeColor, MaterialTheme.shapes.medium)
    } else {
        Modifier
    }

    Box(
        modifier = modifier

            .heightIn(max = 160.dp)
            .then(strokeModifier)
            .clip(MaterialTheme.shapes.large)
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            SelectionContainer {
                BasicTextField(
                    value = text,
                    cursorBrush = SolidColor(Color.White.copy(0.7f)),
                    enabled = enabled,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            onChange(it)
                        }
                    },
                    textStyle = textStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            hasFocus.value = it.isFocused
                            onFocused(it.isFocused)
                        }
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            placeholder?.let { placeholder ->
                                Text(
                                    text = placeholder,
                                    style = textStyle,
                                    color = Color(0xFF909299)
                                )
                            }
                        }
                        if (hasFocus.value || text.isNotEmpty()) {
                            it()
                        }
                    }
                }
            }
        }
    }
}
