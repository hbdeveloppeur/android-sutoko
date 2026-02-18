package com.purpletear.ai_conversation.ui.screens.character.add_character.utility

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText

internal fun filterCharacterName(input: String): String {
    val filteredValue =
        input.filter { it.isLetterOrDigit() || it == '-' || it == ' ' }
    return filteredValue.replace("  ", " ")
}

internal fun visualTransformationCharacterName(input: AnnotatedString): TransformedText {
    val filteredValue = filterCharacterName(input.text)
    return TransformedText(
        AnnotatedString(filteredValue),
        OffsetMapping.Identity
    )
}