package com.purpletear.game.presentation.states

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.game.presentation.R
import com.example.sharedelements.R as SutokoSharedElementsR

/**
 * Sealed class representing the different states of a game menu.
 */
internal sealed class GameMenuState() {
    data object ReadyToPlay : GameMenuState()
    data object Idle : GameMenuState()
    data object DownloadRequired : GameMenuState()

    @Keep
    data class DownloadingGame(val progress: Int) : GameMenuState()
    data object UpdateGameRequired : GameMenuState()
    data object UpdateAppRequired : GameMenuState()
    data object PaymentRequired : GameMenuState()
    data object GameFinished : GameMenuState()

    @Keep
    data class ChapterUnavailable(val number: Int, val createdAt: Long) : GameMenuState()

    @Keep
    data class ConfirmBuy(val isLoading: Boolean = false) : GameMenuState()
    data object ConfirmedBuy : GameMenuState()
    data object Loading : GameMenuState()
    data object LoadingError : GameMenuState()
}

@Keep
internal data class ButtonUiConfig(
    val title: UiText? = null,
    val subtitle: UiText? = null,
    val weight: Float = 0.0001f,
    val backgroundColor: Color = Color(0xFFE71464),
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val icon: Icon? = null,
    val action: StoryPreviewAction? = null,
)

@Keep
internal data class ButtonsUiStates(
    val left: ButtonUiConfig = ButtonUiConfig(),
    val right: ButtonUiConfig
)

private fun restartLeftButton(currentChapterNumber: Int): ButtonUiConfig =
    if (currentChapterNumber <= 1) {
        ButtonUiConfig(
            weight = 0.0001f,
            isEnabled = false,
            isLoading = false,
        )
    } else {
        ButtonUiConfig(
            title = UiText.StringResource(R.string.game_menu_restart),
            weight = 2f,
            action = StoryPreviewAction.OnRestart,
        )
    }

internal fun GameMenuState.buttonConfig(
    currentChapterNumber: Int,
    gamePrice: Int?,
): ButtonsUiStates = when (this) {
    is GameMenuState.ChapterUnavailable -> ButtonsUiStates(
        left = restartLeftButton(currentChapterNumber),

        right = ButtonUiConfig(
            title = UiText.StringResource(R.string.game_menu_play),
            subtitle = UiText.StringResource(
                R.string.game_menu_chapter_number,
                currentChapterNumber
            ),
            weight = 3f,
            isEnabled = false,
        )
    )


    GameMenuState.Idle, GameMenuState.Loading -> {
        ButtonsUiStates(
            left = ButtonUiConfig(
                weight = 2f,
                isEnabled = false,
                isLoading = true,
            ),
            right = ButtonUiConfig(
                weight = 3f,
                isEnabled = false,
                isLoading = true,
            )
        )
    }

    GameMenuState.ReadyToPlay -> {
        ButtonsUiStates(
            left = restartLeftButton(currentChapterNumber),
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_play),
                subtitle = UiText.StringResource(
                    R.string.game_menu_chapter_number,
                    currentChapterNumber
                ),
                weight = 3f,
                action = StoryPreviewAction.OnPlay,
            )
        )
    }

    GameMenuState.GameFinished -> {
        ButtonsUiStates(
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_restart),
                subtitle = UiText.StringResource(R.string.game_menu_game_finished),
                weight = 1f,
            )
        )
    }

    GameMenuState.UpdateAppRequired -> {
        ButtonsUiStates(
            left = restartLeftButton(currentChapterNumber),
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_play),
                subtitle = UiText.StringResource(R.string.game_menu_app_update_required),
                weight = 3f,
                isEnabled = false,
                action = StoryPreviewAction.OnUpdateApp,
            )
        )
    }

    GameMenuState.UpdateGameRequired -> {
        ButtonsUiStates(
            left = restartLeftButton(currentChapterNumber),
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_update_game),
                subtitle = UiText.StringResource(R.string.game_menu_game_update_required),
                weight = 3f,
                isEnabled = true,
                action = StoryPreviewAction.OnUpdateGame,
            )
        )
    }

    is GameMenuState.ConfirmBuy -> {
        ButtonsUiStates(
            left = ButtonUiConfig(
                weight = 1f,
                icon = Icon.Image(
                    SutokoSharedElementsR.drawable.shared_ic_arrow_back_ios,
                ),
                action = StoryPreviewAction.OnAbortBuy,
                isEnabled = !isLoading,
            ),
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_confirm_order),
                subtitle = UiText.StringResource(R.string.game_menu_coins, gamePrice ?: 0),
                weight = 5f,
                backgroundColor = Color(0xFF006FFF),
                action = StoryPreviewAction.OnBuyConfirm,
                isLoading = isLoading,
            )
        )
    }

    GameMenuState.ConfirmedBuy -> {
        ButtonsUiStates(
            right = ButtonUiConfig(
                weight = 1f,
                backgroundColor = Color(0xFF006FFF),
                icon = Icon.LottieAnimation(
                    SutokoSharedElementsR.raw.lottie_check_white,
                    offsetY = -2,
                )
            )
        )
    }

    GameMenuState.DownloadRequired -> {
        ButtonsUiStates(
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_download_game),
                subtitle = UiText.StringResource(R.string.game_menu_download_size),
                weight = 1f,
                action = StoryPreviewAction.OnDownload,
            )
        )
    }

    is GameMenuState.DownloadingGame -> {
        ButtonsUiStates(
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_downloading),
                subtitle = UiText.DynamicText("%d%%".format(progress)),
                backgroundColor = Color(0xFF006FFF),
                weight = 1f,
            )
        )
    }

    GameMenuState.LoadingError -> {
        ButtonsUiStates(
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_reload_game),
                weight = 1f,
                action = StoryPreviewAction.OnReload,
                backgroundColor = Color(0xFF171717),
            )
        )
    }

    GameMenuState.PaymentRequired -> {
        ButtonsUiStates(
            left = restartLeftButton(currentChapterNumber),
            right = ButtonUiConfig(
                title = UiText.StringResource(R.string.game_menu_buy),
                subtitle = UiText.StringResource(R.string.game_menu_buy_to_continue),
                weight = 3f,
                action = StoryPreviewAction.OnBuy,
            )
        )
    }
}
