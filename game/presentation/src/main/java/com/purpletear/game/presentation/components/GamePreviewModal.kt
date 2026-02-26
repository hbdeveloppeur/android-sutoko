package com.purpletear.game.presentation.components

import android.annotation.SuppressLint
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.R as SharedElementsR
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.components.compact.GameCardCompact
import com.purpletear.game.presentation.states.GameButtonsState
import com.purpletear.game.presentation.states.GameState
import com.purpletear.game.presentation.viewmodels.GamePreviewModalViewModel

private val PoppinsMedium = FontFamily(
    Font(SharedElementsR.font.font_poppins_medium, FontWeight.Medium)
)

/**
 * Game preview modal with built-in visibility animation.
 *
 * @param isVisible Whether the modal should be displayed
 * @param onDismiss Called when user taps outside the modal or back is pressed
 * @param gameId The ID of the game to display. Can be null when hidden
 * @param onPlayGame Called when user clicks play - provides the game ID
 * @param onGameDeleted Called when the game is deleted
 * @param modifier Optional modifier for the modal
 */
@Composable
fun GamePreviewModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    gameId: String?,
    onPlayGame: (String) -> Unit = {},
    onGameDeleted: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    viewModel: GamePreviewModalViewModel = hiltViewModel()
) {
    var displayedGameId by remember { mutableStateOf<String?>(null) }

    // Initialize ViewModel when becoming visible with a gameId
    LaunchedEffect(isVisible, gameId) {
        if (isVisible && gameId != null) {
            displayedGameId = gameId
            viewModel.init(gameId)
        }
    }

    // Collect events from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.playGameEvents.collect { id ->
            onPlayGame(id)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.dismissEvents.collect {
            onDismiss()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.gameDeletedEvents.collect {
            onGameDeleted()
            onDismiss()
        }
    }

    // Cleanup when modal is hidden
    DisposableEffect(isVisible) {
        onDispose {
            if (!isVisible) {
                displayedGameId = null
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(200, easing = FastOutSlowInEasing))
    ) {
        val currentGameId = displayedGameId ?: return@AnimatedVisibility
        val game = viewModel.game.value
        val gameState = viewModel.gameState.value

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x81000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            when {
                gameState == GameState.Loading || game == null -> {
                    LoadingContent()
                }

                gameState == GameState.LoadingError -> {
                    ErrorContent(
                        onRetry = { viewModel.init(currentGameId) }
                    )
                }

                else -> {
                    ModalContent(
                        game = game,
                        gameState = gameState,
                        gameButtonsState = viewModel.gameButtonsState,
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

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onRetry() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tap to retry",
            fontFamily = PoppinsMedium,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ModalContent(
    game: com.purpletear.sutoko.game.model.Game,
    gameState: GameState,
    gameButtonsState: GameButtonsState,
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
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 2.dp)
                .padding(bottom = 16.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        // Action Buttons
        GameActionButtons(
            state = gameButtonsState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        Text(
            text= "State: $gameState",
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 2.dp)
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
    }
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

        Box(Modifier.fillMaxSize().background(Color(0x40AD8772)))

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
