package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.sutoko.game.model.game.GameCatalog

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    gameCatalog: GameCatalog,
    premiumIconPainter: Painter? = null,
    boldFontFamily: FontFamily = FontFamily.Default,
    onTap: (GameCatalog) -> Unit,
) {
    Box(
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .background(Color.Black.copy(0.3f))
            .then(modifier)
            .clickable {
                onTap(gameCatalog)
            }) {

        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = gameCatalog.bannerImageRequest(LocalContext.current)
                ?: ImageRequest.Builder(LocalContext.current).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}
