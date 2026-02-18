package fr.purpletear.sutoko.screens.main.presentation.screens.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sutokosharedelements.theme.SutokoTypography
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.custom.PlayerRankInfo
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents


@Composable
fun MembersRankShort(
    viewModel: HomeScreenViewModel
) {
    val state = viewModel.state.value
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // First four loop
            for (i in 0..3) {
                if (i < state.authorsRank.size) {
                    val rank = state.authorsRank[i]
                    MemberCard(
                        playerRank = rank,
                        color = Color(arrayOf(0xFF5B3AE0, 0xFF3A7CE0, 0xFF86CFEE, 0xFF256633)[i])
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(0.10f))
                .clickable {
                    viewModel.onEvent(MainEvents.TapSeeMoreAuthors)
                }, contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.sutoko_see_all_authors),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                style = SutokoTypography.h2.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }

    }
}

@Composable
fun MemberCard(playerRank: PlayerRankInfo, color: Color) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD9D9D9).copy(0.2f),
                        color.copy(0.2f)
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .padding(6.dp)
                .align(
                    Alignment.Center
                )
        ) {
            // Image rounded

            val url = remember {
                if (playerRank.hasPicture) {
                    "https://create.sutoko.app/data/user/profile-picture/${playerRank.id}"
                } else {
                    com.purpletear.smsgame.R.drawable.ic_user_avatar_2
                }
            }

            AsyncImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.2f)),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Text(
                text = playerRank.username,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                style = SutokoTypography.h2.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "TOP ${playerRank.rank} ${if (playerRank.rank < 21) "\uD83E\uDD47" else ""}",
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                style = SutokoTypography.h2.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
    }
}