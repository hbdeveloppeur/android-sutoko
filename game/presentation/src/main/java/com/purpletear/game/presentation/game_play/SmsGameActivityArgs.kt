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
 * @property chapterCode The chapter to start playing.
 * @property isTrial When true, the session is a "try the 1st chapter" trial: at the end of the
 *   chapter the next-chapter CTA is replaced by a buy-to-continue message. Paid, unowned games only.
 * @property autoPlay When true, the story is driven automatically in debug builds: choices,
 *   tap-to-continue, manga pages, and chapter transitions are handled without user input.
 *   Intended for Kimi-cli / QA automation, not end users.
 */
@Keep
@Parcelize
data class SmsGameActivityArgs(
    val gameId: String,
    val chapterCode: String? = null,
    val isTrial: Boolean = false,
    val autoPlay: Boolean = false,
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
         * Extracts the args from the parcelable extra, falling back to plain extras so the
         * activity can be launched from `adb shell am start` with `--es gameId ... --ez autoPlay ...`.
         */
        fun fromIntentOrExtras(intent: Intent): SmsGameActivityArgs? {
            return fromIntent(intent) ?: run {
                val gameId = intent.getStringExtra("gameId") ?: return@run null
                SmsGameActivityArgs(
                    gameId = gameId,
                    chapterCode = intent.getStringExtra("chapterCode"),
                    isTrial = intent.getBooleanExtra("isTrial", false),
                    autoPlay = intent.getBooleanExtra("autoPlay", false),
                )
            }
        }

        /**
         * Creates an Intent with these args as extra.
         */
        fun toIntent(intent: Intent, args: SmsGameActivityArgs): Intent {
            return intent.putExtra(EXTRA_KEY, args)
        }
    }
}
