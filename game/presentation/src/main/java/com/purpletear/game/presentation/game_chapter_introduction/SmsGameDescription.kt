package com.purpletear.game.presentation.game_chapter_introduction

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.SimpleButton
import com.purpletear.game.presentation.game_play.GameSessionViewModel
import com.purpletear.game.presentation.game_play.SmsGameRoutes
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.GameSessionState

internal fun NavGraphBuilder.descriptionScreen(
    viewModel: GameSessionViewModel,
    onContinue: () -> Unit
) = composable(
    route = SmsGameRoutes.DESCRIPTION,
) {
    val state by viewModel.sessionState.collectAsStateWithLifecycle()

    ChapterDescriptionRoute(
        state = state,
        onContinue = onContinue
    )
}

@Composable
private fun ChapterDescriptionRoute(
    state: GameSessionState,
    onContinue: () -> Unit,
) {
    when (state) {
        is GameSessionState.Ready -> ChapterDescriptionContent(
            chapter = state.chapter,
            totalChapters = state.totalChapters,
            onContinue = onContinue
        )

        is GameSessionState.Error -> Box(
            Modifier
                .fillMaxSize()
                .background(Color.Red)
        )

        else -> {}
    }

    AnimatedVisibility(visible = state is GameSessionState.Loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
                    .alpha(1f),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
internal fun ChapterDescriptionContent(
    chapter: Chapter,
    totalChapters: Int,
    onContinue: () -> Unit,
) {
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
            Title(text = "Chapter ${chapter.number}/$totalChapters")
            Subtitle(text = chapter.title)
            Description(text = chapter.description)
            Spacer(modifier = Modifier.size(4.dp))
            SimpleButton(
                text = "Continue",
                onClick = {
                    onContinue()
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
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0920).copy(alpha = 0.2f))
        )
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
    Text(
        text,
        color = Color(0xFFE6E6E6),
        textAlign = TextAlign.Justify,
        lineHeight = 22.sp,
        fontFamily = Poppins
    )
}
