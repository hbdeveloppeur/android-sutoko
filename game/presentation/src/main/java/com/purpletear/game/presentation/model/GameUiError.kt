package com.purpletear.game.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R

sealed interface GameUiError {
    @get:StringRes
    val stringRes: Int

    data object Load : GameUiError {
        override val stringRes = R.string.error_load_game
    }

    data object Purchase : GameUiError {
        override val stringRes = R.string.error_purchase
    }

    data object Download : GameUiError {
        override val stringRes = R.string.error_download
    }

    data object Update : GameUiError {
        override val stringRes = R.string.error_update
    }

    data object Delete : GameUiError {
        override val stringRes = R.string.error_delete
    }
}

@Composable
fun GameUiError.asString(): String = stringResource(stringRes)
