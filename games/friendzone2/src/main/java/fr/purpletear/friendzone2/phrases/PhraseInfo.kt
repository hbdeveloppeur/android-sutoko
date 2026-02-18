/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.phrases

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase

/**
 * Represents a Phrase Dest
 */
class PhraseInfo {

    /**
     * Items id
     */
    companion object {
        var layoutId: Int = R.layout.phrase_info
        var textViewId: Int = R.id.phrase_info_text

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see fr.purpletear.friendzone.activities.main.GameConversationAdapter
         */
        fun design(context: Context, p: Phrase, itemView: View, backgroundImageId: String) {
            val text = itemView.findViewById<TextView>(textViewId)
            text.text = p.sentence
            text.setTextColor(ContextCompat.getColor(context, getColorId(backgroundImageId)))
        }

        /**
         * Get the color id given the background image id
         * @param backgroundImageId
         * @return Int
         */
        fun getColorId(backgroundImageId: String): Int {
            if(backgroundImageId.endsWith("evaphone")) {
                return R.color.white
            }
            return R.color.phrase_info_text
        }
    }
}