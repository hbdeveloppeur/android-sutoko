/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.phrases

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.Phrase
import fr.purpletear.friendzone.tables.TableOfCharacters

/**
 * Represents a Phrase Dest
 */
class PhraseTyping {

    /**
     * Items id
     */
    companion object {
        var backgroundId: Int = R.id.phrase_typing_background

        fun getLayoutId(isSms : Boolean) : Int {
            if(isSms) {
                return R.layout.friendzone1_phrase_typing_sms
            }
            return R.layout.friendzone1_phrase_typing
        }

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see fr.purpletear.friendzone.activities.main.GameConversationAdapter
         */
        fun design(context: Context, characters : TableOfCharacters,  p: Phrase, itemView: View, isSms : Boolean) {
            val c = characters.getCharacter(p.id_author)

            val background = itemView.findViewById<FrameLayout>(backgroundId)
            val gd = background.background as GradientDrawable
            if(!isSms) {
                gd.setColor(ContextCompat.getColor(context, c.colorId))
            } else {
                gd.setColor(ContextCompat.getColor(context, R.color.mainBackgroundSms))
            }
            val i = itemView.findViewById<ImageView>(R.id.phrase_typing_anim)

            val d = c.getTypingAnim(isSms)

            i.setBackgroundResource(d)
            val a = i.background as AnimationDrawable
            a.callback = i
            a.setVisible(true, true)
            a.start()
        }
    }
}