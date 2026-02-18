/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.phrases

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfCharacters

/**
 * Represents a Phrase Dest
 */
class PhraseTyping {

    /**
     * Items id
     */
    companion object {
        var backgroundId: Int = R.id.phrase_typing_background

        fun getLayoutId() : Int {
            return R.layout.phrase_typing
        }

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see fr.purpletear.friendzone.activities.main.GameConversationAdapter
         */
        fun design(context: Context, characters : TableOfCharacters,  p: Phrase, itemView: View) {
            val c = characters.getCharacter(p.id_author)

            val background = itemView.findViewById<FrameLayout>(backgroundId)
            val gd = background.background as GradientDrawable
            gd.setColor(ContextCompat.getColor(context, c.colorId))
            val i = itemView.findViewById<ImageView>(R.id.phrase_typing_anim)

            val d = c.getTypingAnim()

            i.setBackgroundResource(d)
            val a = i.background as AnimationDrawable
            a.callback = i
            a.setVisible(true, true)
            a.start()
        }
    }
}