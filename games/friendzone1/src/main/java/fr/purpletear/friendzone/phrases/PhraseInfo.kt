/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.phrases

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.TextView
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.Phrase

/**
 * Represents a Phrase Dest
 */
class PhraseInfo {

    /**
     * Items id
     */
    companion object {
        var layoutId: Int = R.layout.friendzone1_phrase_info
        var textViewId: Int = R.id.phrase_info_text

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see fr.purpletear.friendzone.activities.main.GameConversationAdapter
         */
        fun design(context: Context, p: Phrase, itemView: View, backgroundImageId: Int) {
            val text = itemView.findViewById<TextView>(textViewId)
            text.text = p.sentence
            text.setTextColor(ContextCompat.getColor(context, getColorId(backgroundImageId)))
        }

        /**
         * Get the color id given the background image id
         * @param backgroundImageId
         * @return Int
         */
        fun getColorId(backgroundImageId: Int): Int {
            return when (backgroundImageId) {
                R.raw.horror -> R.color.softWhite
                R.raw.rain -> R.color.white
                R.drawable.beauty_nature -> R.color.white
                R.drawable.restaurant -> R.color.white
                R.drawable.livingroom -> R.color.white
                R.drawable.glitch4 -> R.color.black
                R.drawable.nightroad -> R.color.softWhite
                R.drawable.stars -> R.color.softWhite
                else -> R.color.phrase_info_text
            }
        }
    }
}