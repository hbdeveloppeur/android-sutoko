package com.purpletear.ai_conversation.ui.component.angular

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
@Preview(name = "AngularLinesBox", showBackground = false, showSystemUi = false)
private fun Preview() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        AngularLinesBox()
    }
}

@Composable
fun AngularLinesBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startY = 28.dp.toPx()
            val endX = size.width - 5.dp.toPx()
            val color = Color.White

            val strokeWidth = 1.dp.toPx()

            // Draw left line
            drawLine(
                color = color,
                start = Offset(5.dp.toPx(), startY),
                end = Offset(5.dp.toPx(), startY + 25.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Draw right line
            drawLine(
                color = color,
                start = Offset(endX, startY),
                end = Offset(endX, startY + 25.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Draw angled left upward
            drawLine(
                color = color,
                start = Offset(5.dp.toPx(), startY),
                end = Offset(21.dp.toPx(), 10.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Draw angled right upward
            drawLine(
                color = color,
                start = Offset(endX, startY),
                end = Offset(size.width - 21.dp.toPx(), 10.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Draw horizontal middle
            drawLine(
                color = color,
                start = Offset(22.dp.toPx(), 10.dp.toPx()),
                end = Offset(size.width - 22.dp.toPx(), 10.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}