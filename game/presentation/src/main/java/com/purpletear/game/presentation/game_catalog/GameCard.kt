package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.model.formatNarrativeThemes
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.isPremium

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

        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Row {
                Text(
                    text = gameCatalog.metadata.title, fontSize = 16.sp,
                    style = TextStyle(
                        letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontFamily = boldFontFamily,
                    color = Color.White,
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(0.7f),
                text = gameCatalog.metadata.catchingPhrase ?: "",
                fontSize = 12.sp,
                style = SutokoTypography.h3.copy(
                    letterSpacing = 0.5.sp,
                    lineHeight = 16.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = Color.White,
            )

            Row {
                Text(
                    modifier = Modifier,
                    text = formatNarrativeThemes(
                        gameCatalog.narrativeThemes,
                        stringResource(R.string.game_card_genre_fallback)
                    ),
                    color = Color.White.copy(0.8f),
                    fontSize = 12.sp,
                    style = SutokoTypography.h3.copy(
                        letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )

                if (gameCatalog.isPremium() && premiumIconPainter != null) {
                    Image(
                        painter = premiumIconPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp, top = 1.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    }
}
