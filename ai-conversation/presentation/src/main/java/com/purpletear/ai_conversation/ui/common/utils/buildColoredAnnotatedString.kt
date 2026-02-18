package com.purpletear.ai_conversation.ui.common.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun buildColoredAnnotatedString(text: String, color: Color = Color(0xFFFF008A)): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("\\*".toRegex())
        if (parts.size % 2 != 0) {
            for (i in parts.indices) {
                if (i % 2 == 0) {
                    append(parts[i])
                } else {
                    withStyle(style = SpanStyle(color = color)) {
                        append(parts[i])
                    }
                }
            }
        } else {
            append(text)
        }
    }
}