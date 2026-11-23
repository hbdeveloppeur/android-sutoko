package com.purpletear.game.presentation.game_preview.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewRestartResult
import com.purpletear.game.presentation.game_preview.GamePreviewRestartTarget
import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.launch

@Composable
fun GamePreviewOptionsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Icon(
            painter = painterResource(R.drawable.game_preview_more_vert),
            contentDescription = stringResource(R.string.game_presentation_game_preview_options),
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePreviewOptionsSheet(
    storyTitle: String,
    chapter: Chapter?,
    isLoadingChapters: Boolean,
    isRestarting: Boolean,
    restartResult: GamePreviewRestartResult?,
    onDismiss: () -> Unit,
    onClearResult: () -> Unit,
    onConfirm: (GamePreviewRestartTarget) -> Unit,
    onRestartFinished: (GamePreviewRestartTarget) -> Unit,
) {
    var target by rememberSaveable { mutableStateOf<GamePreviewRestartTarget?>(null) }
    val busy by rememberUpdatedState(isRestarting)
    val onFinished by rememberUpdatedState(onRestartFinished)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !busy },
    )
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    LaunchedEffect(target) { scrollState.scrollTo(0) }
    val close = { scope.launch { sheetState.hide(); onDismiss() }; Unit }
    val returnToOptions = { target = null; onClearResult() }

    LaunchedEffect(restartResult) {
        if (restartResult is GamePreviewRestartResult.Success) {
            sheetState.hide()
            onFinished(restartResult.target)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isRestarting) onDismiss() },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        containerColor = Color(0xFF10131B),
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f)) },
    ) {
        BackHandler {
            if (!isRestarting) {
                if (target != null) returnToOptions() else close()
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize(tween(180, easing = FastOutSlowInEasing))
                .verticalScroll(scrollState).padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (target != null) {
                    IconButton(onClick = returnToOptions, enabled = !isRestarting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.game_presentation_options_back),
                            tint = Color.White)
                    }
                }
                Column(Modifier.weight(1f).padding(start = if (target == null) 12.dp else 4.dp)) {
                    Text(
                        text = stringResource(R.string.game_presentation_options_title),
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = storyTitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                IconButton(onClick = close, enabled = !isRestarting) {
                    Icon(Icons.Default.Close,
                        contentDescription = stringResource(R.string.game_presentation_options_close),
                        tint = Color.White.copy(alpha = 0.75f))
                }
            }

            val selectedTarget = target
            if (selectedTarget == null) {
                val chapterDetail = when {
                    chapter == null && isLoadingChapters -> stringResource(R.string.game_presentation_options_chapter_loading)
                    chapter == null -> stringResource(R.string.game_presentation_options_chapter_missing)
                    !chapter.available -> stringResource(R.string.game_presentation_options_chapter_unavailable)
                    else -> stringResource(R.string.game_presentation_options_chapter_summary)
                }
                RestartOptionRow(
                    label = stringResource(R.string.game_presentation_options_restart_chapter),
                    detail = chapterDetail,
                    context = chapter?.let { stringResource(R.string.game_presentation_options_chapter_number, it.number) +
                        it.title.takeIf(String::isNotBlank)?.let { title -> " · $title" }.orEmpty() },
                    enabled = chapter?.available == true && !isRestarting,
                    onClick = { onClearResult(); target = GamePreviewRestartTarget.CHAPTER },
                )
                HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = Color.White.copy(alpha = 0.08f))
                RestartOptionRow(
                    label = stringResource(R.string.game_presentation_options_restart_game),
                    detail = stringResource(R.string.game_presentation_options_game_summary),
                    enabled = !isRestarting,
                    onClick = { onClearResult(); target = GamePreviewRestartTarget.GAME },
                )
            } else {
                RestartConfirmation(
                    target = selectedTarget,
                    chapter = chapter,
                    isRestarting = isRestarting,
                    hasError = restartResult is GamePreviewRestartResult.Failure && restartResult.target == selectedTarget,
                    canConfirm = selectedTarget == GamePreviewRestartTarget.GAME || chapter?.available == true,
                    onConfirm = { onConfirm(selectedTarget) },
                    onCancel = returnToOptions,
                )
            }
        }
    }
}

@Composable
private fun RestartOptionRow(label: String, detail: String, enabled: Boolean, onClick: () -> Unit, context: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, fontFamily = PlusJakartaSansFontFamily, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = if (enabled) 1f else 0.5f))
            context?.let { Text(it, fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp, color = Color(0xFFC7D6EF)) }
            Text(detail, fontFamily = PlusJakartaSansFontFamily, fontSize = 13.sp,
                lineHeight = 19.sp, color = Color.White.copy(alpha = 0.65f))
        }
        if (enabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RestartConfirmation(
    target: GamePreviewRestartTarget,
    chapter: Chapter?,
    isRestarting: Boolean,
    hasError: Boolean,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val isChapter = target == GamePreviewRestartTarget.CHAPTER
    val title = if (isChapter && chapter != null) {
        stringResource(R.string.game_presentation_options_confirm_chapter, chapter.number)
    } else stringResource(R.string.game_presentation_game_restart_confirm_title)
    Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, fontFamily = PlusJakartaSansFontFamily, fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
        Text(stringResource(if (isChapter) R.string.game_presentation_options_confirm_chapter_detail
            else R.string.game_presentation_options_confirm_game_detail),
            fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, lineHeight = 22.sp,
            color = Color.White.copy(alpha = 0.75f))
        if (hasError) {
            Text(stringResource(R.string.game_presentation_options_restart_failed),
                color = Color(0xFFFFB4AB), fontSize = 13.sp,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        if (!canConfirm) {
            Text(stringResource(R.string.game_presentation_options_chapter_unavailable),
                color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp)
        }
        Button(
            onClick = onConfirm,
            enabled = canConfirm && !isRestarting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isChapter) Color.White else Color(0xFFF1A5A8),
                contentColor = Color(0xFF10131B),
                disabledContainerColor = Color(0xFF27313E), disabledContentColor = Color.White),
        ) {
            if (isRestarting) {
                CircularProgressIndicator(Modifier.size(18.dp).padding(end = 4.dp), strokeWidth = 2.dp, color = Color.White)
            }
            Text(stringResource(when {
                isRestarting -> R.string.game_presentation_options_restarting
                hasError -> R.string.game_presentation_options_retry_restart
                else -> R.string.game_presentation_game_restart_confirm_button
            }), fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp).semantics { liveRegion = LiveRegionMode.Polite })
        }
        TextButton(onClick = onCancel, enabled = !isRestarting, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(android.R.string.cancel), color = Color.White.copy(alpha = 0.8f), fontFamily = PlusJakartaSansFontFamily)
        }
    }
}
