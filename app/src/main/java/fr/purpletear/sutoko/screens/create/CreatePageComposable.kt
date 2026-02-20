package fr.purpletear.sutoko.screens.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButton
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButtonVariant
import fr.purpletear.sutoko.screens.create.components.section_title.SectionTitle
import fr.purpletear.sutoko.screens.create.components.story_card.StoryCard
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation

private const val BACKGROUND_ALPHA = 0.15f
private const val GRADIENT_TOP_ALPHA = 0.08f
private const val GRADIENT_BOTTOM_ALPHA = 0.00001f

@Composable
internal fun CreatePageComposable(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Background()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(top = 12.dp),
        ) { 
            item {
                TopNavigation(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(start = 8.dp),
                    coins = 960,
                    diamonds = 0,
                    isLoading = false,
                    onAccountButtonPressed = { /* TODO */ },
                    onCoinsButtonPressed = { /* TODO: Navigate to shop */ },
                    onDiamondsButtonPressed = { /* TODO */ },
                    onOptionsButtonPressed = { /* TODO: Show options menu */ }
                )
            }

            item {
                SectionTitle(
                    text = "Histoires de la communauté",
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )
            }

            item {
                StoryCard(
                    title = "The day my life ended",
                    author = "Eva Weeks",
                    imageUrl = "https://data.sutoko.app/resources/sutoko-ai/image/background_waiting_screen.jpg",
                    onGetClick = { /* TODO */ }
                )
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 24.dp))
            }

            item {
                CreateStoryButton(
                    text = "Créer mon histoire",
                    variant = CreateStoryButtonVariant.Violet,
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 12.dp))
            }

            item {
                CreateStoryButton(
                    text = "Voir mes histoires créées",
                    variant = CreateStoryButtonVariant.White,
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }

            item {
                CreateStoryButton(
                    text = "Créer mon histoire",
                    hint = "Se connecter",
                    variant = CreateStoryButtonVariant.Violet,
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
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
