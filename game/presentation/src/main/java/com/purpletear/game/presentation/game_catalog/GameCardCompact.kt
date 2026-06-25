package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.toGameActionState
import com.purpletear.sutoko.game.model.Chapter

private const val CROSSFADE_DURATION_MS = 400

@Composable
fun GameCardCompact(
    isPending: Boolean,
    isPurchasing: Boolean,
    isPurchaseLoading: Boolean,
    currentChapter: Chapter?,
    appBuildNumber: Int,
    isGameFinished: Boolean,

    game: GameItem,

    modifier: Modifier = Modifier,
    showGetButton: Boolean = true,
    openButtonLabel: String? = null,

    onGetClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onCancelClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.1f),
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(game.logoUrl)
                        .crossfade(CROSSFADE_DURATION_MS)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                game.author?.let { author ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = author.displayName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFFFFFFFF).copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (author.isCertified) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_certified_blue),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (showGetButton) {
                GetButton(
                    gameState = game.toGameActionState(
                        isPending = isPending,
                        isPurchasing = isPurchasing,
                        isPurchaseLoading = isPurchaseLoading,
                        currentChapter = currentChapter,
                        appBuildNumber = appBuildNumber,
                        isGameFinished = isGameFinished,
                    ),
                    playButtonLabel = openButtonLabel,
                    onGetClick = onGetClick,
                    onOpenClick = onOpenClick,
                    onCancelClick = onCancelClick
                )
            }
        }
    }
}
