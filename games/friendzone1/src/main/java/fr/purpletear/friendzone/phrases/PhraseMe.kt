/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.phrases

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.Phrase

/**
 * Represents a Phrase Dest
 */
class PhraseMe {

    /**
     * Items id
     */
    companion object {
        var textViewId: Int = R.id.phrase_me_text
        var backgroundId: Int = R.id.phrase_me_background

        fun getLayoutId(isSms : Boolean) : Int {
            if(isSms) {
                return R.layout.friendzone1_phrase_me_sms
            }
            return R.layout.friendzone1_phrase_me
        }

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see GameConversationAdapter
         */
        fun design(context: Context, p: Phrase, itemView: View, isSms: Boolean) {
            val text = itemView.findViewById<TextView>(textViewId)
            val background = itemView.findViewById<FrameLayout>(backgroundId)
            text.text = p.sentence
            val gd = background.background as GradientDrawable
            if(isSms) {
                gd.setColor(ContextCompat.getColor(context, R.color.meBackgroundSms))
            } else {
                gd.setColor(ContextCompat.getColor(context, R.color.meBackground))
                itemView.findViewById<View>(R.id.phrase_me_seen_text).visibility = View.GONE
            }
            text.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }
}