package com.purpletear.game.presentation.game_chapter_introduction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.NickNameInputDialog
import com.purpletear.game.presentation.common.components.SimpleButton
import com.purpletear.game.presentation.common.extensions.toUiString
import com.purpletear.game.presentation.game_play.GameSessionViewModel
import com.purpletear.game.presentation.game_play.SmsGameRoutes
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.GameSessionState

internal fun NavGraphBuilder.descriptionScreen(
    viewModel: GameSessionViewModel,
    onContinue: (String) -> Unit,
    onSelectChapter: () -> Unit,
) = composable(
    route = SmsGameRoutes.DESCRIPTION,
) {
    val state by viewModel.sessionState.collectAsStateWithLifecycle()
    val showRestartDialog by viewModel.showRestartDialog.collectAsStateWithLifecycle()

    ChapterDescriptionRoute(
        viewModel = viewModel,
        state = state,
        onContinue = onContinue,
        onSelectChapter = onSelectChapter,
        onRestart = viewModel::onRestartPressed,
        showRestartDialog = showRestartDialog,
        onRestartDialogConfirm = viewModel::onRestartDialogConfirm,
        onRestartDialogDismiss = viewModel::onRestartDialogDismiss,
    )
}

@Composable
private fun ChapterDescriptionRoute(
    viewModel: GameSessionViewModel,
    state: GameSessionState,
    onContinue: (String) -> Unit,
    onSelectChapter: () -> Unit,
    onRestart: () -> Unit,
    showRestartDialog: Boolean,
    onRestartDialogConfirm: () -> Unit,
    onRestartDialogDismiss: () -> Unit,
) {
    val userNickNameRequired by viewModel.userNickNameRequired.collectAsStateWithLifecycle()
    val heroName by viewModel.heroName.collectAsStateWithLifecycle()
    var showNickNameDialog by remember { mutableStateOf(false) }

    when (state) {
        is GameSessionState.Ready -> {
            val chapter = state.chapter
            val needsNickName = userNickNameRequired && chapter.number == 1 && heroName.isBlank()
            val onContinueToGame = { onContinue(chapter.normalizedCode) }

            ChapterDescriptionContent(
                chapter = chapter,
                onContinue = {
                    if (needsNickName) {
                        showNickNameDialog = true
                    } else {
                        onContinueToGame()
                    }
                },
                onSelectChapter = onSelectChapter,
                onRestart = onRestart,
                showRestartDialog = showRestartDialog,
                onRestartDialogConfirm = onRestartDialogConfirm,
                onRestartDialogDismiss = onRestartDialogDismiss,
            )

            if (showNickNameDialog) {
                NickNameInputDialog(
                    onConfirm = { name ->
                        showNickNameDialog = false
                        viewModel.saveNickName(name)
                        onContinueToGame()
                    },
                    onDismiss = { showNickNameDialog = false },
                )
            }
        }

        is GameSessionState.Error -> Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.game_description_error, state.type.toUiString()),
                color = Color.White
            )
        }

        else -> {}
    }

    AnimatedVisibility(
        visible = state is GameSessionState.Loading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
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
                strokeWidth = 1.dp
            )
        }
    }
}

@Composable
internal fun ChapterDescriptionContent(
    chapter: Chapter,
    onContinue: () -> Unit,
    onSelectChapter: () -> Unit,
    onRestart: () -> Unit,
    showRestartDialog: Boolean = false,
    onRestartDialogConfirm: () -> Unit = {},
    onRestartDialogDismiss: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Background()
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .widthIn(max = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Title(text = stringResource(R.string.game_description_chapter_title, chapter.number))
            Subtitle(text = chapter.title)
            Description(text = chapter.description)
            Spacer(modifier = Modifier.size(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SimpleButton(
                    text = stringResource(R.string.game_description_restart),
                    imageVector = null,
                    backgroundColor = Color(0xFF1B1D22),
                    textColor = Color.White,
                    onClick = {
                        onRestart()
                    }
                )
                SimpleButton(
                    text = stringResource(R.string.game_description_continue),
                    backgroundColor = Color(0xFFFF007A),
                    textColor = Color.White,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onContinue()
                    }
                )
            }

            if (BuildConfig.DEBUG) {
                SimpleButton(
                    text = "Debug: Select a chapter",
                    backgroundColor = Color(0xFF1B1D22),
                    textColor = Color.White,
                    onClick = onSelectChapter
                )
            }
        }

        if (showRestartDialog) {
            SimpleAlertDialog(
                onDismissRequest = onRestartDialogDismiss,
                onConfirmation = onRestartDialogConfirm,
                dialogTitle = stringResource(R.string.game_restart_confirm_title),
                dialogText = stringResource(R.string.game_restart_confirm_description),
                confirmButtonText = stringResource(R.string.game_restart_confirm_button),
                dismissButtonText = stringResource(android.R.string.cancel),
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
