package com.purpletear.game.presentation.game_preview_options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.model.UserRole

private val OptionsBackground = Color(0xFF05070C)
private val OptionsAccent = Color(0xFFFF007A)
private val OptionsDestructive = Color(0xFFFF5252)

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
 * (player/administrator), choose how the story advances, restart the story or
 * delete its memories.
 * Style follows SutokoParamsScreen.
 */
@Composable
fun GamePreviewOptionsScreen(
    viewModel: GamePreviewOptionsViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val role by viewModel.role.collectAsStateWithLifecycle()
    val advanceMode by viewModel.advanceMode.collectAsStateWithLifecycle()
    val currentChapterCode by viewModel.currentChapterCode.collectAsStateWithLifecycle()
    val isFriendzoned by viewModel.isFriendzoned.collectAsStateWithLifecycle()

    // Null until the user edits the field: the current code stays the prefill.
    var chapterCodeInput by rememberSaveable { mutableStateOf<String?>(null) }
    val chapterCode = chapterCodeInput ?: currentChapterCode

    var showRestartDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteMemoriesDialog by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val submitChapterCode = {
        keyboardController?.hide()
        viewModel.onChapterCodeSubmitted(chapterCode)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OptionsBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        OptionsTopBar(onBack = onBack)

        // Friendzoned games manage their own progress: chapter switching here
        // would write a store they never read, so the section is hidden.
        if (!isFriendzoned) {
            OptionsSectionLabel(text = stringResource(R.string.game_presentation_options_chapter_section))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = chapterCode,
                    onValueChange = { chapterCodeInput = sanitizeChapterCodeInput(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.game_presentation_options_chapter_placeholder),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { submitChapterCode() },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = OptionsAccent,
                        focusedBorderColor = OptionsAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.24f),
                    ),
                )
                TextButton(
                    onClick = submitChapterCode,
                    colors = ButtonDefaults.textButtonColors(contentColor = OptionsAccent),
                ) {
                    Text(
                        text = stringResource(R.string.game_presentation_options_chapter_apply),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFontFamily,
                    )
                }
            }
        }

        OptionsSectionLabel(text = stringResource(R.string.game_presentation_options_role_section))
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_role_player),
            selection = role == UserRole.PLAYER,
            onClick = { viewModel.onRoleSelected(UserRole.PLAYER) },
        )
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_role_administrator),
            selection = role == UserRole.ADMINISTRATOR,
            onClick = { viewModel.onRoleSelected(UserRole.ADMINISTRATOR) },
        )

        OptionsSectionLabel(text = stringResource(R.string.game_presentation_options_advance_section))
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_advance_auto),
            subtitle = stringResource(R.string.game_presentation_options_advance_auto_subtitle),
            selection = advanceMode == StoryAdvanceMode.AUTO_PLAY,
            onClick = { viewModel.onAdvanceModeSelected(StoryAdvanceMode.AUTO_PLAY) },
        )
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_advance_click),
            subtitle = stringResource(R.string.game_presentation_options_advance_click_subtitle),
            selection = advanceMode == StoryAdvanceMode.CLICK_TO_ADVANCE,
            onClick = { viewModel.onAdvanceModeSelected(StoryAdvanceMode.CLICK_TO_ADVANCE) },
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.11f),
        )

        OptionsRow(
            label = stringResource(R.string.game_presentation_options_restart_story),
            labelColor = OptionsDestructive,
            onClick = { showRestartDialog = true },
        )
        OptionsRow(
            label = stringResource(R.string.game_presentation_options_delete_memories),
            labelColor = OptionsDestructive,
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
            confirmButtonColor = OptionsDestructive,
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
            confirmButtonColor = OptionsDestructive,
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
                contentDescription = stringResource(R.string.game_presentation_options_back),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.game_presentation_options_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PlusJakartaSansFontFamily,
        )
    }
}

@Composable
private fun OptionsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PlusJakartaSansFontFamily,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 6.dp),
    )
}

/**
 * A settings row. When [selection] is non-null, a radio button shows the
 * selection state of this mutually exclusive option.
 */
@Composable
private fun OptionsRow(
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    selection: Boolean? = null,
    labelColor: Color = Color.White,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSansFontFamily,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (selection != null) {
            RadioButton(
                selected = selection,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = OptionsAccent,
                    unselectedColor = Color.White.copy(alpha = 0.5f),
                ),
            )
        }
    }
}
