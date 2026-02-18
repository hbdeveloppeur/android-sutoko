package com.purpletear.game.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.PlusJakartaSansFontFamily

@Composable
internal fun GamePreviewChapterTitle(modifier: Modifier = Modifier, text: String) {
    AnnotatedText(
        modifier = modifier,
        text = text,
        fontFamily = PlusJakartaSansFontFamily,
        color = Color.White.copy(0.8f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}


@Composable
private fun AnnotatedText(
    modifier: Modifier = Modifier,
    text: String,
    fontFamily: FontFamily = PlusJakartaSansFontFamily,
    color: Color,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        modifier = modifier
            .graphicsLayer {
                translationY = -4.dp.toPx()
            },
        text = buildColoredAnnotatedString(text),
        fontSize = fontSize,
        lineHeight = 18.sp,
        color = color,
        textAlign = TextAlign.Justify,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        style = TextStyle(
            hyphens = Hyphens.Auto
        ),
        softWrap = true
    )
}

private fun buildColoredAnnotatedString(
    text: String,
    color: Color = Color(0xFFFFFFFF)
): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("\\*".toRegex())
        if (parts.size % 2 != 0) {
            for (i in parts.indices) {
                if (i % 2 == 0) {
                    append(parts[i])
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = color,
                            fontFamily = FontFamily(
                                Font(
                                    com.example.sharedelements.R.font.font_worksans_semibold,
                                    FontWeight.SemiBold
                                )
                            ),
                        )
                    ) {
                        append(parts[i])
                    }
                }
            }
        } else {
            append(text)
        }
    }
}