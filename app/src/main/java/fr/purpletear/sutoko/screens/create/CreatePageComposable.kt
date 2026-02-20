package fr.purpletear.sutoko.screens.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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

import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButton
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButtonVariant
import fr.purpletear.sutoko.screens.create.components.load_more_button.LoadMoreButton
import fr.purpletear.sutoko.screens.create.components.search_box.SearchBox
import fr.purpletear.sutoko.screens.create.components.section_title.SectionTitle
import fr.purpletear.sutoko.screens.create.components.story_card.StoryCard
import fr.purpletear.sutoko.screens.create.components.story_cover.StoryCover
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
) {
    val focusManager = LocalFocusManager.current
    val balance by viewModel.balance

    val coins = when (balance) {
        is com.purpletear.core.presentation.extensions.Resource.Success -> {
            (balance as com.purpletear.core.presentation.extensions.Resource.Success).data?.coins
                ?: viewModel.getCoins()
        }
        else -> viewModel.getCoins()
    }

    val diamonds = when (balance) {
        is com.purpletear.core.presentation.extensions.Resource.Success -> {
            (balance as com.purpletear.core.presentation.extensions.Resource.Success).data?.diamonds
                ?: viewModel.getDiamonds()
        }
        else -> viewModel.getDiamonds()
    }

    val isLoading = balance is com.purpletear.core.presentation.extensions.Resource.Loading

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
                    isLoading = isLoading,
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

            item {
                StoryCover(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    coverUrl = "https://media.discordapp.net/attachments/1450792285590786139/1474433761499156638/image_1.png?ex=6999d4f2&is=69988372&hm=6f37cc265d99d5e4d96215e354293587b0f233d82fca1b8ac8910f63722206e3&=&format=webp&quality=lossless&width=1024&height=520",
                    title = "The day my life ended",
                    author = "Eva Weeks",
                    thumbnailUrl = "https://media.discordapp.net/attachments/1450792285590786139/1474416156881191044/tmp_logo.png?ex=6999c48d&is=6998730d&hm=f013ec27a0d7f540a4ad6dcee2ee14962995a419199df41646fe5a7e147b4140&=&format=webp&quality=lossless&width=132&height=132"
                )
            }

            item {
                CreateStoryButton(
                    text = "Créer mon histoire",
                    variant = CreateStoryButtonVariant.Violet,
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
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
            /**
             *
             *
             *             item {
             *                 Spacer(modifier = Modifier.padding(bottom = 12.dp))
             *             }
             *
             *             item {
             *                 CreateStoryButton(
             *                     text = "Voir mes histoires créées",
             *                     variant = CreateStoryButtonVariant.White,
             *                     onClick = { /* TODO */ },
             *                     modifier = Modifier.padding(horizontal = 16.dp)
             *                 )
             *             }
             *
             *             item {
             *                 Spacer(modifier = Modifier.padding(bottom = 16.dp))
             *             }
             *
             *             item {
             *                 CreateStoryButton(
             *                     text = "Créer mon histoire",
             *                     hint = "Se connecter",
             *                     variant = CreateStoryButtonVariant.Violet,
             *                     onClick = { /* TODO */ },
             *                     modifier = Modifier.padding(horizontal = 16.dp)
             *                 )
             *             }
             */

            item {
                StoryCard(
                    modifier = Modifier.padding(top = 16.dp),
                    title = "The day my life ended",
                    author = "Eva Weeks",
                    imageUrl = "https://media.discordapp.net/attachments/1450792285590786139/1474416156881191044/tmp_logo.png?ex=6999c48d&is=6998730d&hm=f013ec27a0d7f540a4ad6dcee2ee14962995a419199df41646fe5a7e147b4140&=&format=webp&quality=lossless&width=132&height=132",
                    onGetClick = { /* TODO */ }
                )
            }
            item {
                LoadMoreButton(
                    onClick = { /* TODO: Load more stories */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
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
