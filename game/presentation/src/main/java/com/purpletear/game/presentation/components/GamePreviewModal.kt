package com.purpletear.game.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.purpletear.game.presentation.states.ButtonUiState
import com.purpletear.game.presentation.states.GameButtonsState
import com.example.sharedelements.utils.UiText
import com.purpletear.game.presentation.states.GameState
import com.purpletear.game.presentation.states.StoryPreviewAction
import com.purpletear.game.presentation.states.toButtonsState
import com.purpletear.sutoko.game.model.Game

private val PoppinsMedium = FontFamily(
    Font(com.example.sharedelements.R.font.font_poppins_medium, FontWeight.Medium)
)

/**
 * Game preview modal with built-in visibility animation.
 *
 * @param isVisible Whether the modal should be displayed
 * @param onDismiss Called when user taps outside the modal or back is pressed
 * @param game The game to display. Can be null when hidden (preserved for exit animation)
 */
@Composable
fun GamePreviewModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    game: Game?,
    gameState: GameState,
    modifier: Modifier = Modifier,
    onAction: (StoryPreviewAction) -> Unit,
) {
    var displayedGame by remember { mutableStateOf<Game?>(null) }
    
    // Update displayed game when becoming visible
    LaunchedEffect(isVisible, game) {
        if (isVisible && game != null) displayedGame = game
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(200, easing = FastOutSlowInEasing))
    ) {
        val currentGame = displayedGame ?: return@AnimatedVisibility
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xCB151313))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            ModalContent(
                game = currentGame,
                gameState = gameState,
                onAction = onAction,
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

@Composable
private fun ModalContent(
    game: Game,
    gameState: GameState,
    onAction: (StoryPreviewAction) -> Unit,
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
        // Banner with overlapping Game Info
        BannerWithGameInfo(
            bannerUrl = game.bannerAsset?.let { "https://sutoko.com/media/${it.storagePath}" } ?: "",
            thumbnailUrl = game.logoAsset?.let { "https://sutoko.com/media/${it.thumbnailStoragePath}" } ?: "",
            title = game.metadata.title,
            author = game.author?.displayName ?: "",
            isAuthorCertified = game.author?.isCertified ?: false
        )

        // Description
        Text(
            text = game.metadata.description ?: "",
            fontFamily = PoppinsMedium,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp).padding(bottom = 16.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        // Action Buttons
        ModalActionButtons(
            gameState = gameState,
            onAction = onAction,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun ModalActionButtons(
    gameState: GameState,
    onAction: (StoryPreviewAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonsState = gameState.toButtonsState(
        currentChapterNumber = 1,
        gamePrice = null,
        onAction = onAction,
    )

    GameActionButtons(
        state = buttonsState,
        modifier = modifier,
    )
}

@Composable
private fun BannerWithGameInfo(
    bannerUrl: String,
    thumbnailUrl: String,
    title: String,
    author: String,
    isAuthorCertified: Boolean
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        // Banner Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(bannerUrl)
                .crossfade(400)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(Modifier.fillMaxSize().background(Color(0x60AD8772)))

        // Bottom gradient for better text visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 1f)
                        )
                    )
                )
        )

        // Game Info overlaid at bottom
        GameCardCompact(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp),
            title = title,
            author = author,
            imageUrl = thumbnailUrl,
            isAuthorCertified = isAuthorCertified,
            showGetButton = false
        )
    }
}


