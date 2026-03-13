package com.purpletear.game.presentation.smsgame

import android.content.Intent
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.Data
import com.purpletear.game.presentation.extensions.getParcelableExtraCompat
import kotlinx.parcelize.Parcelize

/**
 * Arguments/Parameters for launching SmsGameActivity.
 *
 * @property gameId The unique identifier of the game to be played
 */
@Keep
@Parcelize
data class SmsGameActivityArgs(
    val gameId: String
) : Parcelable {
    companion object {
        private val EXTRA_KEY = Data.Companion.Extra.SMS_GAME_MODEL.id

        /**
         * Extracts the args from the intent extras.
         *
         * @param intent The intent to extract from
         * @return The SmsGameActivityArgs or null if not found
         */
        fun fromIntent(intent: Intent): SmsGameActivityArgs? {
            return intent.getParcelableExtraCompat(EXTRA_KEY)
        }

        /**
         * Creates an Intent with these args as extra.
         */
        fun toIntent(intent: Intent, args: SmsGameActivityArgs): Intent {
            return intent.putExtra(EXTRA_KEY, args)
        }
    }
}
