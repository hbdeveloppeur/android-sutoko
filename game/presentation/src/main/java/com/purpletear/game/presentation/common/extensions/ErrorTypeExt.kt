package com.purpletear.game.presentation.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.ErrorType

@Composable
internal fun ErrorType.toUiString(): String = stringResource(
    when (this) {
        ErrorType.GAME_NOT_FOUND -> R.string.error_game_not_found
        ErrorType.GAME_NOT_INSTALLED -> R.string.error_game_not_installed
        ErrorType.GAME_UPDATE_REQUIRED -> R.string.error_game_update_required
        ErrorType.CHAPTER_UNAVAILABLE -> R.string.error_chapter_unavailable
        ErrorType.CHAPTER_NOT_FOUND -> R.string.error_chapter_not_found
        ErrorType.NO_CHAPTERS_FOUND -> R.string.error_no_chapters_found
        ErrorType.UNKNOWN -> R.string.error_unknown
    }
)
