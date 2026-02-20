package fr.purpletear.sutoko.screens.create.components.search_box

import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sharedelements.theme.Poppins

private val SearchBoxBackground = Color(0xFF2D2D2D)
private val SearchBoxBorderFocused = Color(0xFF4A4A4A)
private val SearchBoxBorderUnfocused = Color(0xFF2D2D2D)
private val IconColorFocused = Color.White
private val IconColorUnfocused = Color.White.copy(alpha = 0.5f)
private val PlaceholderColor = Color.White.copy(alpha = 0.4f)
private val TextColor = Color.White
private val ClearButtonColor = Color.White.copy(alpha = 0.5f)

@Composable
internal fun SearchBox(
    modifier: Modifier = Modifier,
    placeholder: String = "Un auteur, une titre, un thème",
    onSearch: (String) -> Unit = {},
    onValueChange: (String) -> Unit = {}
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isClearable = textState.text.isNotEmpty()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val view = LocalView.current

    var lastKeyboardHeight by remember { mutableStateOf(0) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            val keyboardHeight = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
            
            // Keyboard was visible and now hidden
            if (lastKeyboardHeight > 0 && keyboardHeight == 0) {
                focusManager.clearFocus()
            }
            lastKeyboardHeight = keyboardHeight
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SearchBoxBackground)
            .border(
                width = 1.dp,
                color = if (isFocused) SearchBoxBorderFocused else SearchBoxBorderUnfocused,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = textState,
            onValueChange = { newValue ->
                val sanitized = sanitizeSearchInput(textState.text, newValue.text)
                if (sanitized != newValue.text) {
                    textState = newValue.copy(text = sanitized)
                } else {
                    textState = newValue
                }
                onValueChange(textState.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = TextColor,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            cursorBrush = SolidColor(TextColor),
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch(textState.text)
                }
            ),
            decorationBox = { innerTextField ->
                SearchBoxDecoration(
                    innerTextField = innerTextField,
                    placeholder = placeholder,
                    isFocused = isFocused,
                    isClearable = isClearable,
                    onClear = {
                        textState = TextFieldValue("")
                        onValueChange("")
                        focusManager.clearFocus()
                    }
                )
            }
        )
    }
}

@Composable
private fun SearchBoxDecoration(
    innerTextField: @Composable () -> Unit,
    placeholder: String,
    isFocused: Boolean,
    isClearable: Boolean,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchIcon(isFocused = isFocused)

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (!isFocused && isClearable.not()) {
                Text(
                    text = placeholder,
                    color = PlaceholderColor,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
            innerTextField()
        }

        ClearButton(
            visible = isClearable,
            onClear = onClear
        )
    }
}

@Composable
private fun SearchIcon(isFocused: Boolean) {
    val iconColor = if (isFocused) IconColorFocused else IconColorUnfocused

    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ClearButton(
    visible: Boolean,
    onClear: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Clear search",
            tint = ClearButtonColor,
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClear
                )
        )
    }
}

private fun sanitizeSearchInput(currentText: String, newText: String): String {
    // Prevent leading space
    if (newText.isNotEmpty() && newText.first() == ' ' && currentText.isEmpty()) {
        return ""
    }
    
    // Prevent double space
    if (newText.length > currentText.length && newText.endsWith("  ")) {
        return newText.trimEnd()
    }
    
    return newText
}
