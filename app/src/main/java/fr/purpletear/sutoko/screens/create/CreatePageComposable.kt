package fr.purpletear.sutoko.screens.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.R
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopNavigation(
                coins = 960,
                diamonds = 0,
                isLoading = false,
                onAccountButtonPressed = { /* TODO */ },
                onCoinsButtonPressed = { /* TODO: Navigate to shop */ },
                onDiamondsButtonPressed = { /* TODO */ },
                onOptionsButtonPressed = { /* TODO: Show options menu */ }
            )
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
