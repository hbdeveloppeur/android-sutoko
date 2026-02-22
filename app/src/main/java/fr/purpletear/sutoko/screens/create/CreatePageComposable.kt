package fr.purpletear.sutoko.screens.create

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButton
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButtonVariant
import fr.purpletear.sutoko.screens.create.components.game_card.GameCard
import fr.purpletear.sutoko.screens.create.components.game_cover.GameCover
import fr.purpletear.sutoko.screens.create.components.load_more_button.LoadMoreButton
import fr.purpletear.sutoko.screens.create.components.search_box.SearchBox
import fr.purpletear.sutoko.screens.create.components.section_title.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation

private const val BACKGROUND_ALPHA = 0.15f
private const val GRADIENT_TOP_ALPHA = 0.08f
private const val GRADIENT_BOTTOM_ALPHA = 0.00001f

@Composable
internal fun CreatePageComposable(
    modifier: Modifier = Modifier,
    viewModel: CreateViewModel = hiltViewModel(),
    onAccountButtonPressed: () -> Unit = {},
    onCoinsButtonPressed: () -> Unit = {},
    onDiamondsButtonPressed: () -> Unit = {},
    onOptionsButtonPressed: () -> Unit = {},
    onGameClick: (Game) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val balance by viewModel.balance
    val userGames by viewModel.userGames
    val isLoadingMore by viewModel.isLoadingMore
    val hasMorePages by viewModel.hasMorePages

    val targetCoins = when (balance) {
        is Resource.Success -> {
            (balance as Resource.Success).data?.coins
                ?: viewModel.getCoins()
        }
        else -> viewModel.getCoins()
    }

    val targetDiamonds = when (balance) {
        is Resource.Success -> {
            (balance as Resource.Success).data?.diamonds
                ?: viewModel.getDiamonds()
        }
        else -> viewModel.getDiamonds()
    }

    val coins by animateIntAsState(
        targetValue = targetCoins,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 200f
        ),
        label = "coins_animation"
    )

    val diamonds by animateIntAsState(
        targetValue = targetDiamonds,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 200f
        ),
        label = "diamonds_animation"
    )

    val isBalanceLoading = balance is Resource.Loading
    val games = (userGames as? Resource.Success)?.data.orEmpty()
    val isGamesLoading = userGames is Resource.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(.5f))
    ) {
        Background()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(top = 12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            item {
                TopNavigation(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(start = 8.dp),
                    coins = coins,
                    diamonds = diamonds,
                    isLoading = isBalanceLoading,
                    onAccountButtonPressed = onAccountButtonPressed,
                    onCoinsButtonPressed = onCoinsButtonPressed,
                    onDiamondsButtonPressed = onDiamondsButtonPressed,
                    onOptionsButtonPressed = onOptionsButtonPressed
                )
            }

            item {
                SectionTitle(
                    text = "Histoires de la communauté",
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )
            }

            // Featured game cover (first game or placeholder)
            item {
                val featuredGame = games.firstOrNull()
                GameCover(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    coverUrl = featuredGame?.bannerAsset?.let { "https://sutoko.com/media/${it.storagePath}" }
                        ?: "https://media.discordapp.net/attachments/1450792285590786139/1474433761499156638/image_1.png?ex=6999d4f2&is=69988372&hm=6f37cc265d99d5e4d96215e354293587b0f233d82fca1b8ac8910f63722206e3&=&format=webp&quality=lossless&width=1024&height=520",
                    title = featuredGame?.metadata?.title ?: "The day my life ended",
                    author = featuredGame?.author?.displayName ?: "Eva Weeks",
                    thumbnailUrl = featuredGame?.logoAsset?.let { "https://sutoko.com/media/${it.thumbnailStoragePath}" }
                        ?: "https://media.discordapp.net/attachments/1450792285590786139/1474416156881191044/tmp_logo.png?ex=6999c48d&is=6998730d&hm=f013ec27a0d7f540a4ad6dcee2ee14962995a419199df41646fe5a7e147b4140&=&format=webp&quality=lossless&width=132&height=132"
                )
            }

            item {
                CreateStoryButton(
                    text = "Créer mon histoire",
                    variant = CreateStoryButtonVariant.Violet,
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
            }

            item {
                SearchBox(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    onSearch = { query ->
                        // TODO: Handle search
                    },
                    onValueChange = { query ->
                        // TODO: Handle search query change
                    }
                )
            }

            // Loading state
            if (isGamesLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Games list
            items(
                items = games,
                key = { it.id }
            ) { game ->
                GameCard(
                    modifier = Modifier.padding(top = 16.dp),
                    title = game.metadata.title,
                    author = game.author?.displayName ?: "",
                    imageUrl = game.logoAsset?.let { "https://sutoko.com/media/${it.thumbnailStoragePath}" }
                        ?: "",
                    onGetClick = { onGameClick(game) }
                )
            }

            // Load more button
            if (hasMorePages || isLoadingMore) {
                item {
                    LoadMoreButton(
                        onClick = { viewModel.loadMoreUserGames() },
                        isLoading = isLoadingMore,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
            }

            // Bottom spacer for better scrolling experience
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Background() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(R.drawable.book_details_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(BACKGROUND_ALPHA)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4DB9EC).copy(alpha = GRADIENT_TOP_ALPHA),
                            Color(0xFF4DB9EC).copy(alpha = GRADIENT_BOTTOM_ALPHA),
                        )
                    )
                )
        )
    }
}
