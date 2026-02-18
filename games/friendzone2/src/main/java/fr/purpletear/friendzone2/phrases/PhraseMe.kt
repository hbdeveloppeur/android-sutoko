/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.phrases

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfCharacters

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

        fun getLayoutId() : Int {
            return R.layout.friendzone2_phrase_me
        }

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see GameConversationAdapter
         */
        fun design(context: Context, p: Phrase, characters: TableOfCharacters, itemView: View, isNightMode : Boolean, isPhoneMode : Boolean) {
            val character = characters.getCharacter(p.id_author)

            val text = itemView.findViewById<TextView>(textViewId)
            val background = itemView.findViewById<FrameLayout>(backgroundId)
            text.text = p.sentence
            val gd = background.background as GradientDrawable

            if(isPhoneMode) {
                gd.setColor(ContextCompat.getColor(context, R.color.friendzone2_phone_background_me))
                itemView.findViewById<View>(R.id.phrase_me_seen_text).visibility = View.GONE
                text.setTextColor(ContextCompat.getColor(context, R.color.friendzone2_phone_text_me))
                return
            }

            gd.setColor(ContextCompat.getColor(context, if(isNightMode) R.color.phrase_me_night_mode_background else character.colorId))
            itemView.findViewById<View>(R.id.phrase_me_seen_text).visibility = View.GONE
            text.setTextColor(ContextCompat.getColor(context, if(isNightMode) R.color.phrase_me_night_mode_text else character.textColorId))
        }
    }
}