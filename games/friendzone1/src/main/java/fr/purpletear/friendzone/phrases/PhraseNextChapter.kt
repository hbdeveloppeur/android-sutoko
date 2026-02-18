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

/**
 * Represents a Phrase Dest
 */
class PhraseNextChapter {

    /**
     * Items id
     */
    companion object {
        val layoutId: Int = R.layout.friendzone1_phrase_next_chapter
        val textViewId: Int = R.id.phrase_next_chapter_text

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see GameConversationAdapter
         */
        fun design(context: Context, itemView: View, backgroundMediaId : Int) {
            val text = itemView.findViewById<TextView>(textViewId)
            text.text = context.getString(R.string.mainactivity_next_chapter)
            text.setTextColor(ContextCompat.getColor(context, PhraseInfo.getColorId(backgroundMediaId)))
        }
    }
}