package com.purpletear.game.presentation.smsgame.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.smsgame.SmsGameRoutes
import com.purpletear.game.presentation.smsgame.SmsGameViewModel
import com.purpletear.sutoko.game.model.Chapter

@Preview
@Composable
private fun Preview() {
    SmsGameDescription(number = 1, totalChapters = 5, title = "Title", description = "Description", onContinueButtonClicked = {})
}

internal fun NavGraphBuilder.descriptionScreen(
    gameId: String,
    totalChapters: Int,
    onContinue: () -> Unit
) = composable(
    route = SmsGameRoutes.DESCRIPTION,
    arguments = listOf(navArgument("chapterCode") { type = NavType.StringType })
) { backStackEntry ->
    val chapterCode = backStackEntry.arguments?.getString("chapterCode") ?: ""
    val viewModel: SmsGameViewModel = hiltViewModel()
    val chapter by viewModel.getChapter(gameId, chapterCode).collectAsStateWithLifecycle(initialValue = null)
    
    if (chapter != null) {
        SmsGameDescription(
            number = chapter!!.number,
            totalChapters = totalChapters,
            title = chapter!!.title,
            description = chapter!!.description,
            onContinueButtonClicked = onContinue,
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
internal fun SmsGameDescription(number: Int, totalChapters: Int, title: String, description: String, onContinueButtonClicked: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Background()
        Column(
            modifier = Modifier.widthIn(max = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Title(text = "Chapter $number/$totalChapters")
            Subtitle(text = title)
            Description(text = description)
            Spacer(modifier = Modifier.size(4.dp))
            SimpleButton(
                text = "Continue",
                onClick = {
                    onContinueButtonClicked()
                }
            )
        }
    }
}

@Composable
private fun Background() {
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = R.drawable.game_smsgame_introduction_background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color(0xFF0F0920).copy(alpha = 0.2f)))
    }
}

@Composable
private fun Title(text: String) {
    Text(text, color = Color(0xFF8C8C8C), fontFamily = Poppins, fontSize = 12.sp)
}

@Composable
private fun Subtitle(text: String) {
    Text(text, color = Color.White, fontFamily = Poppins, fontSize = 13.sp)
}

@Composable
private fun Description(text: String) {
    Text(text, color = Color(0xFFE6E6E6), textAlign = TextAlign.Justify, lineHeight = 22.sp, fontFamily = Poppins)
}