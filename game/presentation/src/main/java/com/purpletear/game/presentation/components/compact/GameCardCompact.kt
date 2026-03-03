package com.purpletear.game.presentation.components.compact

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.states.GameState

private const val CROSSFADE_DURATION_MS = 400

/**
 * A compact game card component that displays game information with a Get/Open button.
 * The button behaves like the Apple App Store "Get" button with states:
 * - Get: Game not installed, click to download
 * - Circular Progress: Downloading with progress (0-100%)
 * - Indeterminate Progress: Extracting files
 * - Open: Game installed and ready to play
 * - Retry: Error occurred during download
 *
 * @param modifier Modifier for the card
 * @param title Game title
 * @param author Author name
 * @param imageUrl URL for the game icon/image
 * @param isAuthorCertified Whether to show the certified badge
 * @param showGetButton Whether to show the Get/Open button
 * @param gameState Current game state (Idle, DownloadingGame, ReadyToPlay, etc.)
 * @param onGetClick Called when user clicks Get/Retry to start download
 * @param onOpenClick Called when user clicks Open to launch the game
 * @param onCancelClick Called when user clicks to cancel ongoing download (optional)
 * @param onClick Called when the card itself is clicked (for preview modal)
 */
@Composable
fun GameCardCompact(
    modifier: Modifier = Modifier,
    title: String,
    author: String,
    imageUrl: String,
    isAuthorCertified: Boolean = false,
    showGetButton: Boolean = true,
    gameState: GameState = GameState.Idle,
    onGetClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onCancelClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
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
                        .data(imageUrl)
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
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = author,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFFFFFFFF).copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isAuthorCertified) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_certified_blue),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (showGetButton) {
                GetButton(
                    gameState = gameState,
                    onGetClick = onGetClick,
                    onOpenClick = onOpenClick,
                    onCancelClick = onCancelClick
                )
            }
        }

        // DEBUG: Show game state (only in debug builds)
        if (BuildConfig.DEBUG) {
            val stateText = when (gameState) {
                is GameState.DownloadingGame -> "Downloading ${gameState.progress}%"
                GameState.ReadyToPlay -> "ReadyToPlay"
                GameState.Idle -> "Idle"
                GameState.DownloadRequired -> "DownloadRequired"
                GameState.UpdateGameRequired -> "UpdateGameRequired"
                GameState.UpdateAppRequired -> "UpdateAppRequired"
                GameState.PaymentRequired -> "PaymentRequired"
                GameState.GameFinished -> "GameFinished"
                is GameState.ChapterUnavailable -> "ChapterUnavailable #${gameState.number}"
                is GameState.ConfirmBuy -> "ConfirmBuy"
                GameState.ConfirmedBuy -> "ConfirmedBuy"
                GameState.Loading -> "Loading"
                GameState.LoadingError -> "LoadingError"
            }
            Text(
                text = "[DEBUG] $stateText",
                fontFamily = Poppins,
                fontSize = 10.sp,
                color = Color(0xFF4DB9EC),
                modifier = Modifier.padding(top = 4.dp, start = 62.dp)
            )
        }
    }
}
