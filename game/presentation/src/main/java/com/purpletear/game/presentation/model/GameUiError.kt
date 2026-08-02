package com.purpletear.game.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

sealed interface GameUiError {
    @get:StringRes
    val stringRes: Int

    data object Load : GameUiError {
        override val stringRes = R.string.game_presentation_error_load_game
    }

    data object Purchase : GameUiError {
        override val stringRes = R.string.game_presentation_error_purchase
    }

    data object Download : GameUiError {
        override val stringRes = R.string.game_presentation_error_download
    }

    data object DownloadForbidden : GameUiError {
        override val stringRes = R.string.game_presentation_error_download_forbidden
    }

    data object DownloadUnknown : GameUiError {
        override val stringRes = R.string.game_presentation_error_download_unknown
    }

    data object Update : GameUiError {
        override val stringRes = R.string.game_presentation_error_update
    }

    data object Delete : GameUiError {
        override val stringRes = R.string.game_presentation_error_delete
    }

    data object Restart : GameUiError {
        override val stringRes = R.string.game_presentation_error_restart
    }

    companion object {
        /**
         * Maps a download failure to an honest user-facing error.
         * Network failures -> [Download] (connection message), missing entitlement
         * -> [DownloadForbidden], anything else (server 5xx, corrupt zip, disk) -> [DownloadUnknown].
         */
        fun fromDownloadError(error: Throwable): GameUiError {
            var cause: Throwable? = error
            while (cause != null) {
                when (cause) {
                    is GameDownloadForbiddenException -> return DownloadForbidden
                    is UnknownHostException,
                    is SocketTimeoutException,
                    is ConnectException,
                    is SSLException -> return Download
                }
                cause = cause.cause
            }
            return DownloadUnknown
        }
    }
}

@Composable
fun GameUiError.asString(): String = stringResource(stringRes)
