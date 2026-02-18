package com.purpletear.ai_conversation.ui.common.utils

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent

fun Modifier.logRedraws(tag: String): Modifier = this.then(
    Modifier.drawWithContent {
        Log.d("Compose Drawer", "[$tag] Composable draw finished.")
        drawContent()
    }
)