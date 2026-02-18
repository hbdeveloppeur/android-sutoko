package com.purpletear.ai_conversation.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SecondCounter(startTimestamp: Long, initialSeconds: Double = 0.0) {
    var elapsedSeconds by remember { mutableDoubleStateOf(initialSeconds) }

    LaunchedEffect(startTimestamp) {
        while (true) {
            elapsedSeconds = (System.currentTimeMillis() - startTimestamp) / 1000.0
            delay(16L)
        }
    }

    val displayText = if (elapsedSeconds > 20) {
        "20+"
    } else {
        String.format(Locale.current.platformLocale, "%.2f", elapsedSeconds)
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 10.sp
    )
}
