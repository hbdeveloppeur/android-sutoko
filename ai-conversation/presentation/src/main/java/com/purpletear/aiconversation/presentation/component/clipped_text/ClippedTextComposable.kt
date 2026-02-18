package com.purpletear.aiconversation.presentation.component.clipped_text

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun ClippedTextComposable(
    message: String,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    SubcomposeLayout(modifier) { constraints ->
        val composable = @Composable { localOnTextLayout: (TextLayoutResult) -> Unit ->
            SelectionContainer {
                Text(
                    message,
                    onTextLayout = localOnTextLayout,
                    color = Color.White,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .widthIn(max = 170.dp, min = 40.dp),
                    lineHeight = 18.sp,
                )
            }
        }
        var textWidthOpt: Int? = null
        subcompose("measureView") {
            composable { layoutResult ->
                textWidthOpt = (0 until layoutResult.lineCount)
                    .maxOf { line ->
                        ceil(layoutResult.getLineRight(line) - layoutResult.getLineLeft(line)).toInt()
                    }
            }
        }[0].measure(constraints)
        val textWidth = textWidthOpt!!
        val placeable = subcompose("content") {
            composable(onTextLayout)
        }[0].measure(constraints.copy(minWidth = textWidth, maxWidth = textWidth))

        layout(width = textWidth, height = placeable.height) {
            placeable.place(0, 0)
        }
    }
}