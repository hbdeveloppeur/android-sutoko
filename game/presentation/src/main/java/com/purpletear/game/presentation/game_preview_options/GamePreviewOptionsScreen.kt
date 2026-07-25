package com.purpletear.game.presentation.game_preview_options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.UserRole

private val OptionsBackground = Color(0xFF05070C)
private val OptionsAccent = Color(0xFFFF007A)

/**
 * Sanitizes raw chapter code input: removes spaces, forces uppercase and
 * rejects any value not starting with a digit.
 */
private fun sanitizeChapterCodeInput(input: String): String {
    val cleaned = input.filter { !it.isWhitespace() }.uppercase()
    return if (cleaned.isEmpty() || cleaned.first().isDigit()) cleaned else ""
}

/**
 * Developer options of a story: set the current chapter by code, pick a role
 * (player/administrator), restart the story or delete its memories.
 * Style follows SutokoParamsScreen with smaller fonts.
 */
@Composable
fun GamePreviewOptionsScreen(
    viewModel: GamePreviewOptionsViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val role by viewModel.role.collectAsStateWithLifecycle()
    val currentChapterCode by viewModel.currentChapterCode.collectAsStateWithLifecycle()

    // Null until the user edits the field: the current code stays the prefill.
    var chapterCodeInput by rememberSaveable { mutableStateOf<String?>(null) }
    val chapterCode = chapterCodeInput ?: currentChapterCode

    var showRestartDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteMemoriesDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OptionsBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        OptionsTopBar(onBack = onBack)

        OptionsSectionLabel(text = stringResource(R.string.game_presentation_options_chapter_section))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = chapterCode,
                onValueChange = { chapterCodeInput = sanitizeChapterCodeInput(it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.onChapterCodeSubmitted(chapterCode) },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = OptionsAccent,
                    focusedBorderColor = OptionsAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.24f),
                ),
            )
            Text(
                text = stringResource(R.string.game_presentation_options_chapter_apply),
                color = OptionsAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PlusJakartaSansFontFamily,
                modifier = Modifier.clickable { viewModel.onChapterCodeSubmitted(chapterCode) },
            )
        }

        OptionsSectionLabel(text = stringResource(R.string.game_presentation_options_role_section))
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_role_player),
            selected = role == UserRole.PLAYER,
            onClick = { viewModel.onRoleSelected(UserRole.PLAYER) },
        )
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_role_administrator),
            selected = role == UserRole.ADMINISTRATOR,
            onClick = { viewModel.onRoleSelected(UserRole.ADMINISTRATOR) },
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.11f),
        )

        OptionsRow(
            label = stringResource(R.string.game_presentation_options_restart_story),
            onClick = { showRestartDialog = true },
        )
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_delete_memories),
            onClick = { showDeleteMemoriesDialog = true },
        )
    }

    if (showRestartDialog) {
        SimpleAlertDialog(
            onDismissRequest = { showRestartDialog = false },
            onConfirmation = {
                showRestartDialog = false
                viewModel.onRestartConfirmed()
            },
            dialogTitle = stringResource(R.string.game_presentation_game_restart_confirm_title),
            dialogText = stringResource(R.string.game_presentation_game_restart_confirm_description),
            confirmButtonText = stringResource(R.string.game_presentation_game_restart_confirm_button),
            dismissButtonText = stringResource(android.R.string.cancel),
        )
    }

    if (showDeleteMemoriesDialog) {
        SimpleAlertDialog(
            onDismissRequest = { showDeleteMemoriesDialog = false },
            onConfirmation = {
                showDeleteMemoriesDialog = false
                viewModel.onDeleteMemoriesConfirmed()
            },
            dialogTitle = stringResource(R.string.game_presentation_options_delete_memories_confirm_title),
            dialogText = stringResource(R.string.game_presentation_options_delete_memories_confirm_description),
            confirmButtonText = stringResource(R.string.game_presentation_options_delete_memories),
            dismissButtonText = stringResource(android.R.string.cancel),
        )
    }
}

@Composable
private fun OptionsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = stringResource(R.string.game_presentation_options_title),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
        )
    }
}

@Composable
private fun OptionsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PlusJakartaSansFontFamily,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun OptionsRow(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = PlusJakartaSansFontFamily,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = OptionsAccent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
