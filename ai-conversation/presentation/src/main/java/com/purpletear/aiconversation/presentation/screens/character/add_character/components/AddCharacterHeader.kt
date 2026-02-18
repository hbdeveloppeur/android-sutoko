package com.purpletear.aiconversation.presentation.screens.character.add_character.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Preview(name = "AddCharacterHeader", showBackground = false, showSystemUi = false)
@Composable
private fun Preview() {
    AddCharacterHeader()
}

@Composable
internal fun AddCharacterHeader(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D111B).copy(0f), Color(0xFF0D111B).copy(1f))
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(modifier)
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://data.sutoko.app/resources/sutoko-ai/image/header_add_character.jpeg")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = gradient
                )
        )
    }
}