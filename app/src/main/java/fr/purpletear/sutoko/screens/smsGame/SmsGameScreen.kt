package fr.purpletear.sutoko.screens.smsgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.sutoko.game.model.GameSessionState

@Composable
fun SmsGameScreen(
    sessionState: GameSessionState,
    onRetry: () -> Unit
) {
    HideStatusBarEffect()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when (val state = sessionState) {
            is GameSessionState.Loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }

            is GameSessionState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = onRetry
                )
            }

            is GameSessionState.Ready -> {
                GameScreen(
                    gameTitle = state.chapter.title,
                    chapterNumber = state.chapter.number,
                    heroName = state.heroName
                )
            }
        }
    }
}

@Composable
private fun HideStatusBarEffect() {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = true
        onDispose {
            systemUiController.isStatusBarVisible = true
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun GameScreen(
    gameTitle: String,
    chapterNumber: Int,
    heroName: String
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = gameTitle.ifEmpty { "Game" },
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chapter $chapterNumber",
            color = Color.Gray
        )
        if (heroName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hero: $heroName",
                color = Color.Gray
            )
        }
    }
}