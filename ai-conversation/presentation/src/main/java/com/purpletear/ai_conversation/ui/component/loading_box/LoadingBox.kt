package com.purpletear.ai_conversation.ui.component.loading_box

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp


@Composable
fun LoadingBox() {
    val alphaBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF05070C).copy(0.6f),
            Color(0xFF0E1116).copy(0.6f)
        )
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(alphaBrush)
            .pointerInput(Unit) {}
            .focusable(true)

    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center),
            color = Color.LightGray,
            strokeWidth = 2.dp
        )
    }
}
