package com.purpletear.game.presentation.common.states

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.utils.UiText
import com.example.sharedelements.utils.UiText.DynamicText
import com.example.sharedelements.utils.UiText.StringResource
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.Icon.Image
import com.purpletear.core.presentation.components.icon.Icon.LottieAnimation
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewAction
import com.purpletear.game.presentation.model.GameAction
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
internal fun GameAction?.toButtonsState(
    onAction: (GamePreviewAction) -> Unit,
): GameButtonsState = when (this) {
    is GameAction.Play -> GameButtonsState(
        left = restartLeftButton(this.chapterNumber, onAction),
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_play),
            subtitle = StringResource(
                R.string.game_menu_chapter_number,
                this.chapterNumber
            ),
            weight = 3f,
            isEnabled = false,
            onClick = { onAction(GamePreviewAction.OnPlay) },
        )
    )

    null -> GameButtonsState(
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

    is GameAction.GameFinished -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_restart),
            subtitle = StringResource(R.string.game_menu_game_finished),
            weight = 3f,
            onClick = { onAction(GamePreviewAction.OnRestart) },
        )
    )

    is GameAction.UpdateApp -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_play),
            subtitle = StringResource(R.string.game_menu_app_update_required),
            weight = 3f,
            isEnabled = false,
            onClick = { onAction(GamePreviewAction.OnUpdateApp) },
        )
    )

    is GameAction.UpdateGame -> GameButtonsState(
        left = deleteLeftButton(onAction),
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_update_game),
            subtitle = StringResource(R.string.game_menu_game_update_required),
            weight = 3f,
            isEnabled = true,
            onClick = { onAction(GamePreviewAction.OnUpdateGame) },
        )
    )

    is GameAction.ConfirmPurchase -> {
        if (isBought) {
            GameButtonsState(
                right = ButtonUiState(
                    weight = 1f,
                    backgroundColor = Color(0xFF006FFF),
                    icon = LottieAnimation(
                        SutokoSharedElementsR.raw.lottie_check_white,
                        offsetY = -2,
                    )
                )
            )
        } else {
            GameButtonsState(
                left = ButtonUiState(
                    weight = 2f,
                    icon = Image(
                        SutokoSharedElementsR.drawable.shared_ic_arrow_back_ios,
                    ),
                    onClick = { onAction(GamePreviewAction.OnAbortBuy) },
                    isEnabled = true,
                ),
                right = ButtonUiState(
                    weight = 8f,
                    title = StringResource(R.string.game_menu_confirm_order),
                    subtitle = StringResource(R.string.game_menu_coins, 960),
                    backgroundColor = Color(0xFF006FFF),
                    onClick = { onAction(GamePreviewAction.OnBuyConfirm) },
                    isLoading = false,
                )
            )
        }
    }

    is GameAction.Download -> GameButtonsState(
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_download_game),
            subtitle = StringResource(R.string.game_menu_download_size),
            weight = 1f,
            onClick = { onAction(GamePreviewAction.OnDownload) },
        )
    )

    is GameAction.Downloading -> GameButtonsState(
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_downloading),
            subtitle = DynamicText("%d%%".format(progress)),
            backgroundColor = Color(0xFF006FFF),
            weight = 1f,
        )
    )

    is GameAction.Purchase -> GameButtonsState(
        left = restartLeftButton(1, onAction),
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_buy),
            subtitle = StringResource(R.string.game_menu_buy_to_continue),
            weight = 3f,
            onClick = { onAction(GamePreviewAction.OnBuy) },
        )
    )

    is GameAction.Pending -> GameButtonsState(
        right = ButtonUiState(
            title = StringResource(R.string.game_menu_pending),
            subtitle = StringResource(R.string.game_menu_pending_subtitle),
            backgroundColor = Color(0xFF006FFF),
            weight = 1f,
        )
    )
}

private fun deleteLeftButton(
    onAction: (GamePreviewAction) -> Unit,
): ButtonUiState = ButtonUiState(
    title = UiText.StringResource(R.string.game_menu_delete),
    weight = 2f,
    onClick = { onAction(GamePreviewAction.OnDelete) },
)

private fun restartLeftButton(
    currentChapterNumber: Int,
    onAction: (GamePreviewAction) -> Unit,
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
        onClick = { onAction(GamePreviewAction.OnRestart) },
    )
}
