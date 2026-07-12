package com.purpletear.game.presentation.common.states

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.utils.UiText
import com.example.sharedelements.utils.UiText.StringResource
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.Icon.Image
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewAction
import com.purpletear.game.presentation.model.GameActionState
import com.example.sharedelements.R as SutokoSharedElementsR

/**
 * UI-specific state for the game action buttons.
 * This is a plain data class that can be used by any component without ViewModel dependency.
 */
@Keep
internal data class GameButtonsState(
    val left: ButtonUiState = ButtonUiState(),
    val right: ButtonUiState = ButtonUiState(),
)

@Keep
internal data class ButtonUiState(
    val title: UiText? = null,
    val subtitle: UiText? = null,
    val weight: Float = HIDDEN_WEIGHT,
    val backgroundColor: Color = RightButtonBackground,
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val icon: Icon? = null,
    val onClick: (() -> Unit)? = null,
)

private const val LEFT_BUTTON_WEIGHT = 2f
private const val RIGHT_BUTTON_WEIGHT = 3f
private const val HIDDEN_WEIGHT = 0.0001f
private val LeftButtonBackground = Color(0xFF191919)
private val RightButtonBackground = Color(0xFFE71464)
private val InfoBlue = Color(0xFF006FFF)

/**
 * Maps GameActionState to GameButtonsState based on the current game context.
 */
internal fun GameActionState?.toButtonsState(
    onAction: (GamePreviewAction) -> Unit,
): GameButtonsState = when (this) {
    null -> GameButtonsState(
        left = loadingButton(HIDDEN_WEIGHT),
        right = loadingButton(RIGHT_BUTTON_WEIGHT),
    )

    is GameActionState.Play -> GameButtonsState(
        left = restartLeftButton(chapterNumber, onAction),
        right = primaryRightButton(
            onAction = onAction,
            title = StringResource(R.string.game_menu_play),
            subtitle = StringResource(R.string.game_menu_chapter_number, chapterNumber),
            action = GamePreviewAction.OnPlay,
            isEnabled = isChapterAvailable,
        ),
    )

    is GameActionState.GameFinished -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = primaryRightButton(
            onAction = onAction,
            title = StringResource(R.string.game_menu_restart),
            subtitle = StringResource(R.string.game_menu_game_finished),
            action = GamePreviewAction.OnRestart,
        ),
    )

    is GameActionState.UpdateApp -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = primaryRightButton(
            onAction = onAction,
            title = StringResource(R.string.game_menu_play),
            subtitle = StringResource(R.string.game_menu_app_update_required),
            action = GamePreviewAction.OnUpdateApp,
            isEnabled = false,
        ),
    )

    is GameActionState.UpdateGame -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = primaryRightButton(
            onAction = onAction,
            title = StringResource(R.string.game_menu_update_game),
            subtitle = StringResource(R.string.game_menu_game_update_required),
            action = GamePreviewAction.OnUpdateGame,
        ),
    )

    is GameActionState.ConfirmPurchase -> GameButtonsState(
        left = ButtonUiState(
            weight = LEFT_BUTTON_WEIGHT,
            icon = Image(SutokoSharedElementsR.drawable.shared_ic_arrow_back_ios),
            backgroundColor = LeftButtonBackground,
            isEnabled = !isLoading,
            onClick = { onAction(GamePreviewAction.OnAbortBuy) },
        ),
        right = ButtonUiState(
            weight = 8f,
            title = StringResource(R.string.game_menu_confirm_order),
            subtitle = StringResource(R.string.game_menu_coins, this.price),
            backgroundColor = InfoBlue,
            isLoading = isLoading,
            onClick = { onAction(GamePreviewAction.OnBuyConfirm) },
        ),
    )

    is GameActionState.Download -> GameButtonsState(
        right = infoRightButton(
            onAction = onAction,
            title = R.string.game_menu_download_game,
            subtitle = R.string.game_menu_download_size,
            action = GamePreviewAction.OnDownload,
        ),
    )

    is GameActionState.Downloading -> GameButtonsState(
        right = ButtonUiState(
            weight = 1f,
            title = StringResource(R.string.game_menu_downloading),
            subtitle = StringResource(R.string.game_menu_download_progress_percent, progress.toInt()),
            backgroundColor = InfoBlue,
        ),
    )

    is GameActionState.Purchase -> GameButtonsState(
        left = if (showTry) {
            // Try + Buy share the row equally (1:1) when both are shown.
            tryFirstChapterLeftButton(onAction, weight = 1f)
        } else {
            restartLeftButton(chapterNumber, onAction)
        },
        right = primaryRightButton(
            onAction = onAction,
            title = StringResource(R.string.game_menu_buy),
            subtitle = if (isUserConnected) {
                StringResource(R.string.game_menu_coins, price)
            } else {
                StringResource(R.string.game_menu_connect_to_buy)
            },
            action = GamePreviewAction.OnBuy,
            weight = if (showTry) 1f else RIGHT_BUTTON_WEIGHT,
        ),
    )

    is GameActionState.Pending -> GameButtonsState(
        right = infoRightButton(
            onAction = onAction,
            title = R.string.game_menu_pending,
            subtitle = R.string.game_menu_pending_subtitle,
        ),
    )
}

private fun deleteLeftButton(
    onAction: (GamePreviewAction) -> Unit,
): ButtonUiState = ButtonUiState(
    title = StringResource(R.string.game_menu_delete),
    weight = LEFT_BUTTON_WEIGHT,
    backgroundColor = LeftButtonBackground,
    onClick = { onAction(GamePreviewAction.OnDelete) },
)

private fun restartLeftButton(
    currentChapterNumber: Int,
    onAction: (GamePreviewAction) -> Unit,
): ButtonUiState = if (currentChapterNumber <= 1) {
    hiddenButton()
} else {
    ButtonUiState(
        title = StringResource(R.string.game_menu_restart),
        weight = LEFT_BUTTON_WEIGHT,
        backgroundColor = LeftButtonBackground,
        onClick = { onAction(GamePreviewAction.OnRestart) },
    )
}

private fun tryFirstChapterLeftButton(
    onAction: (GamePreviewAction) -> Unit,
    weight: Float = LEFT_BUTTON_WEIGHT,
): ButtonUiState = ButtonUiState(
    title = StringResource(R.string.game_menu_try_first_chapter),
    subtitle = StringResource(R.string.game_menu_try_first_chapter_subtitle),
    weight = weight,
    backgroundColor = LeftButtonBackground,
    onClick = { onAction(GamePreviewAction.OnTry) },
)

private fun hiddenButton(): ButtonUiState = ButtonUiState(
    weight = HIDDEN_WEIGHT,
    isEnabled = false,
    backgroundColor = LeftButtonBackground,
)

private fun loadingButton(weight: Float): ButtonUiState = ButtonUiState(
    weight = weight,
    isEnabled = false,
    isLoading = true,
)

private fun primaryRightButton(
    onAction: (GamePreviewAction) -> Unit,
    title: StringResource,
    subtitle: StringResource? = null,
    action: GamePreviewAction,
    isEnabled: Boolean = true,
    backgroundColor: Color = RightButtonBackground,
    weight: Float = RIGHT_BUTTON_WEIGHT,
): ButtonUiState = ButtonUiState(
    title = title,
    subtitle = subtitle,
    weight = weight,
    backgroundColor = backgroundColor,
    isEnabled = isEnabled,
    onClick = { onAction(action) },
)

private fun infoRightButton(
    onAction: (GamePreviewAction) -> Unit,
    title: Int,
    subtitle: Int,
    action: GamePreviewAction? = null,
    backgroundColor: Color = InfoBlue,
    weight: Float = 1f,
): ButtonUiState = ButtonUiState(
    title = StringResource(title),
    subtitle = StringResource(subtitle),
    weight = weight,
    backgroundColor = backgroundColor,
    onClick = action?.let { { onAction(it) } },
)
