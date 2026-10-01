package fr.purpletear.sutoko.screens.main.presentation.screens.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/** Fast cache reads show content directly instead of flashing a skeleton for a frame. */
@Composable
internal fun rememberHomeLoadingVisible(isLoading: Boolean): Boolean {
    var visible by remember(isLoading) { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(120)
            visible = true
        }
    }
    return isLoading && visible
}
