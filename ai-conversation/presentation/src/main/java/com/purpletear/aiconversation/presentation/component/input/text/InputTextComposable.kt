package com.purpletear.aiconversation.presentation.component.input.text

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.screens.character.add_character.utility.filterCharacterName
import com.purpletear.aiconversation.presentation.screens.character.add_character.utility.visualTransformationCharacterName
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "InputTextComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_form_input),
                contentDescription = null,
            )
            InputTextComposable(
                Modifier.fillMaxWidth(0.92f),
                label = "Name",
                value = "Eva",
                isErrorEnabled = false,
                onValueChange = {}
            )
        }
    }
}


@Composable
internal fun InputTextComposable(
    modifier: Modifier = Modifier,
    label: String,
    isErrorEnabled: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onClickErrorBadge: () -> Unit = {}
) {

    Row(
        modifier
            .background(Color(0xFF111B27), shape = MaterialTheme.shapes.medium)
            .border(1.dp, Color(0xFF537399), MaterialTheme.shapes.medium)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .padding(start = 12.dp)
                .widthIn(min = 60.dp),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFFFFFFFF).copy(0.4f))
        )
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Enter a value...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            val customTextSelectionColors = TextSelectionColors(
                handleColor = Color(0xFF537399),
                backgroundColor = Color(0xFF81DEEA).copy(0.3f)
            )
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {

                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        onValueChange(filterCharacterName(newValue))
                    },
                    visualTransformation = { text ->
                        visualTransformationCharacterName(text)
                    },
                    textStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White),
                    cursorBrush = SolidColor(Color.White.copy(0.7f)),
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Ascii
                    )
                )
            }
        }

        if (isErrorEnabled) {
            Image(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(22.dp)
                    .clickable { onClickErrorBadge() },
                painter = painterResource(id = R.drawable.ic_alert),
                contentDescription = null,
            )
        }
    }
}