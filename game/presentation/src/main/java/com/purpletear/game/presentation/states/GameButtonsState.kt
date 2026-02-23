package com.purpletear.game.presentation.states

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.game.presentation.R
import com.example.sharedelements.R as SutokoSharedElementsR

/**
 * UI-specific state for the game action buttons.
 * This is a plain data class that can be used by any component without ViewModel dependency.
 */
@Keep
internal data class GameButtonsState(
    val left: ButtonUiState = ButtonUiState(),
    val right: ButtonUiState = ButtonUiState(),
    val shouldTriggerVibration: Boolean = false,
)

@Keep
internal data class ButtonUiState(
    val title: UiText? = null,
    val subtitle: UiText? = null,
    val weight: Float = 0.0001f,
    val backgroundColor: Color = Color(0xFFE71464),
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val icon: Icon? = null,
    val onClick: (() -> Unit)? = null,
)

/**
 * Maps GameState to GameButtonsState based on the current game context.
 */
internal fun GameState.toButtonsState(
    currentChapterNumber: Int,
    gamePrice: Int?,
    onAction: (StoryPreviewAction) -> Unit,
): GameButtonsState = when (this) {
    is GameState.ChapterUnavailable -> GameButtonsState(
        left = restartLeftButton(currentChapterNumber, onAction),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_play),
            subtitle = UiText.StringResource(
                R.string.game_menu_chapter_number,
                currentChapterNumber
            ),
            weight = 3f,
            isEnabled = false,
        )
    )

    GameState.Idle, GameState.Loading -> GameButtonsState(
        left = ButtonUiState(
            weight = 2f,
            isEnabled = false,
            isLoading = true,
        ),
        right = ButtonUiState(
            weight = 3f,
            isEnabled = false,
            isLoading = true,
        )
    )

    GameState.ReadyToPlay -> GameButtonsState(
        left = restartLeftButton(currentChapterNumber, onAction),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_play),
            subtitle = UiText.StringResource(
                R.string.game_menu_chapter_number,
                currentChapterNumber
            ),
            weight = 3f,
            onClick = { onAction(StoryPreviewAction.OnPlay) },
        )
    )

    GameState.GameFinished -> GameButtonsState(
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_restart),
            subtitle = UiText.StringResource(R.string.game_menu_game_finished),
            weight = 1f,
            onClick = { onAction(StoryPreviewAction.OnRestart) },
        )
    )

    GameState.UpdateAppRequired -> GameButtonsState(
        left = restartLeftButton(currentChapterNumber, onAction),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_play),
            subtitle = UiText.StringResource(R.string.game_menu_app_update_required),
            weight = 3f,
            isEnabled = false,
            onClick = { onAction(StoryPreviewAction.OnUpdateApp) },
        )
    )

    GameState.UpdateGameRequired -> GameButtonsState(
        left = restartLeftButton(currentChapterNumber, onAction),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_update_game),
            subtitle = UiText.StringResource(R.string.game_menu_game_update_required),
            weight = 3f,
            isEnabled = true,
            onClick = { onAction(StoryPreviewAction.OnUpdateGame) },
        )
    )

    is GameState.ConfirmBuy -> GameButtonsState(
        left = ButtonUiState(
            weight = 1f,
            icon = Icon.Image(
                SutokoSharedElementsR.drawable.shared_ic_arrow_back_ios,
            ),
            onClick = { onAction(StoryPreviewAction.OnAbortBuy) },
            isEnabled = !isLoading,
        ),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_confirm_order),
            subtitle = UiText.StringResource(R.string.game_menu_coins, gamePrice ?: 0),
            weight = 5f,
            backgroundColor = Color(0xFF006FFF),
            onClick = { onAction(StoryPreviewAction.OnBuyConfirm) },
            isLoading = isLoading,
        )
    )

    GameState.ConfirmedBuy -> GameButtonsState(
        right = ButtonUiState(
            weight = 1f,
            backgroundColor = Color(0xFF006FFF),
            icon = Icon.LottieAnimation(
                SutokoSharedElementsR.raw.lottie_check_white,
                offsetY = -2,
            )
        )
    )

    GameState.DownloadRequired -> GameButtonsState(
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_download_game),
            subtitle = UiText.StringResource(R.string.game_menu_download_size),
            weight = 1f,
            onClick = { onAction(StoryPreviewAction.OnDownload) },
        )
    )

    is GameState.DownloadingGame -> GameButtonsState(
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_downloading),
            subtitle = UiText.DynamicText("%d%%".format(progress)),
            backgroundColor = Color(0xFF006FFF),
            weight = 1f,
        )
    )

    GameState.LoadingError -> GameButtonsState(
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_reload_game),
            weight = 1f,
            onClick = { onAction(StoryPreviewAction.OnReload) },
            backgroundColor = Color(0xFF171717),
        )
    )

    GameState.PaymentRequired -> GameButtonsState(
        left = restartLeftButton(currentChapterNumber, onAction),
        right = ButtonUiState(
            title = UiText.StringResource(R.string.game_menu_buy),
            subtitle = UiText.StringResource(R.string.game_menu_buy_to_continue),
            weight = 3f,
            onClick = { onAction(StoryPreviewAction.OnBuy) },
        )
    )
}

private fun restartLeftButton(
    currentChapterNumber: Int,
    onAction: (StoryPreviewAction) -> Unit,
): ButtonUiState = if (currentChapterNumber <= 1) {
    ButtonUiState(
        weight = 0.0001f,
        isEnabled = false,
        isLoading = false,
    )
} else {
    ButtonUiState(
        title = UiText.StringResource(R.string.game_menu_restart),
        weight = 2f,
        onClick = { onAction(StoryPreviewAction.OnRestart) },
    )
}
