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
 * @property showDescription When false, the activity starts directly on the game screen.
 *                          Intended for GamePreview, where the description is already shown.
 * @property chapterCode Required when [showDescription] is false; ignored otherwise.
 *                       If null while [showDescription] is false, the activity falls back
 *                       to the description screen to avoid launching without a chapter.
 */
@Keep
@Parcelize
data class SmsGameActivityArgs(
    val gameId: String,
    val storyId: String? = null,
    val isLiveUpdateMode: Boolean = false,
    val showDescription: Boolean = true,
    val chapterCode: String? = null,
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
