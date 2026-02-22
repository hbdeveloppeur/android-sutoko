package com.purpletear.game.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.components.compact.GameCardCompact
import com.purpletear.sutoko.game.model.Game

@Composable
fun GamePreviewModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    game: Game?,
    modifier: Modifier = Modifier,
    onRestartClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
) {
    val animationDuration = 450

    // Remember the displayed game to allow exit animation even when game becomes null
    var displayedGame by remember { mutableStateOf<Game?>(null) }

    // Track if we're in the process of dismissing to handle animation properly
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = isVisible

    // Update displayedGame when modal becomes visible with a valid game
    LaunchedEffect(isVisible, game) {
        if (isVisible && game != null) {
            displayedGame = game
        }
    }

    // Only show the modal if it's visible or in the process of exiting (to allow animation)
    if (visibleState.currentState || visibleState.targetState) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            ),
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = FastOutSlowInEasing
                        )
                    ),
                ) {
                    displayedGame?.let {
                        ModalContent(
                            game = it,
                            onRestartClick = onRestartClick,
                            onDownloadClick = onDownloadClick,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { /* Prevent dismiss when clicking content */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalContent(
    game: Game,
    onRestartClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Banner Image
        BannerImage(
            bannerUrl = game.bannerAsset?.let { "https://sutoko.com/media/${it.storagePath}" } ?: ""
        )

        // Game Info Section
        GameInfoSection(
            thumbnailUrl = game.logoAsset?.let { "https://sutoko.com/media/${it.thumbnailStoragePath}" } ?: "",
            title = game.metadata.title,
            author = game.author?.displayName ?: "",
            isAuthorCertified = game.author?.isCertified ?: false
        )

        // Description
        Text(
            text = game.metadata.description ?: "",
            fontFamily = FontFamily(Font(com.example.sharedelements.R.font.font_worksans_regular, FontWeight.Normal)),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Recommencer",
                onClick = onRestartClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Télécharger",
                onClick = onDownloadClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BannerImage(bannerUrl: String) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(bannerUrl)
                .crossfade(400)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient for better text visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun GameInfoSection(
    thumbnailUrl: String,
    title: String,
    author: String,
    isAuthorCertified: Boolean
) {
    GameCardCompact(
        modifier = Modifier.padding(top = 12.dp),
        title = title,
        author = author,
        imageUrl = thumbnailUrl,
        isAuthorCertified = isAuthorCertified,
        showGetButton = false
    )
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF333333))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily(Font(com.example.sharedelements.R.font.font_worksans_semibold, FontWeight.SemiBold)),
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
