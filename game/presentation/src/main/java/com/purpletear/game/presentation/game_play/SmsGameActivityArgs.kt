package com.purpletear.game.presentation.game_play

import android.content.Intent
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.Data
import com.purpletear.game.presentation.common.extensions.getParcelableExtraCompat
import kotlinx.parcelize.Parcelize

/**
 * Arguments/Parameters for launching SmsGameActivity.
 *
 * @property gameId The unique identifier of the game to be played
 * @property storyId Optional story identifier for real-time author testing mode.
 * @property isLiveUpdateMode When true, the activity connects to a backend test session.
 * @property chapterCode The chapter to start playing. Required unless [isLiveUpdateMode] is true.
 * @property isTrial When true, the session is a "try the 1st chapter" trial: at the end of the
 *   chapter the next-chapter CTA is replaced by a buy-to-continue message. Paid, unowned games only.
 */
@Keep
@Parcelize
data class SmsGameActivityArgs(
    val gameId: String,
    val storyId: String? = null,
    val isLiveUpdateMode: Boolean = false,
    val chapterCode: String? = null,
    val isTrial: Boolean = false,
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
