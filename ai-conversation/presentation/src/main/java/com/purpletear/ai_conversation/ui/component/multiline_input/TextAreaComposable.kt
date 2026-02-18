package com.purpletear.ai_conversation.ui.component.multiline_input

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme

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
            TextAreaComposable(
                text = "",
                modifier = Modifier
                    .fillMaxWidth(0.92f),
                placeholder = "Description...",
                subPlaceholder = "Write your description here",
                backgroundColor = Color(0xFF111B27),
                strokeColor = Color(0xFF537399)
            )
        }
    }
}

@Composable
fun TextAreaComposable(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    strokeColor: Color? = null,
    placeholder: String? = null,
    subPlaceholder: String? = null,
    displayCount: Boolean = false,
    onChange: (text: String) -> Unit = {}
) {

    val hasFocus = remember { mutableStateOf(false) }
    val maxChar = 250
    val charCount = text.length
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

            .heightIn(max = 120.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .then(strokeModifier)
            .clip(MaterialTheme.shapes.large)
            .padding(16.dp)
    ) {
        Column {
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                BasicTextField(
                    value = text,
                    cursorBrush = SolidColor(Color.White.copy(0.7f)),
                    onValueChange = {
                        if (it.length <= maxChar) {
                            onChange(it)
                        }
                    },

                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onFocusChanged {
                            hasFocus.value = it.isFocused
                            //onFocused(it.isFocused)
                        }
                ) {
                    if (text.isEmpty() && hasFocus.value.not()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            placeholder?.let { placeholder ->
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF909299)
                                )
                            }
                            subPlaceholder?.let { subPlaceholder ->
                                Text(
                                    text = subPlaceholder,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF646771)
                                )
                            }
                        }
                    } else {
                        it()
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayCount) {
                Text(
                    text = "$charCount / $maxChar",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}