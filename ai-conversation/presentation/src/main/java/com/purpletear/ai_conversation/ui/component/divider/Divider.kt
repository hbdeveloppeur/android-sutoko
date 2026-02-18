package com.purpletear.ai_conversation.ui.component.divider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DividerComposable(modifier: Modifier = Modifier) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(1f)
                .align(Alignment.Center),
            thickness = 1.dp,
            color = Color.White.copy(0.1f)
        )
    }
}