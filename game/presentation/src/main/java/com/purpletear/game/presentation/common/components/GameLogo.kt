package com.purpletear.game.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Displays the game's logo (title asset), scaled to fit without distortion.
 * Falls back to the readable story title when the asset is missing or unavailable.
 */
@Composable
internal fun GameLogo(
    titleUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    title: String? = contentDescription,
) {
    var imageFailed by remember(titleUrl) { mutableStateOf(false) }
    if (titleUrl.isNullOrBlank() || imageFailed) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                modifier = modifier,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        return
    }
    val context = LocalContext.current
    val request = remember(titleUrl) {
        ImageRequest.Builder(context)
            .data(titleUrl)
            .crossfade(true)
            .build()
    }
    AsyncImage(
        modifier = modifier,
        model = request,
        contentScale = ContentScale.Fit,
        contentDescription = contentDescription,
        onError = { imageFailed = true },
    )
}
