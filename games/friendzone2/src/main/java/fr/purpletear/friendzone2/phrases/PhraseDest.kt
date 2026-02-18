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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfCharacters

/**
 * Represents a Phrase Dest
 */
class PhraseDest {

    /**
     * Itms id
     */
    companion object {
        var textViewId: Int = R.id.phrase_dest_text
        var backgroundId: Int = R.id.phrase_dest_background
        var imageId: Int = R.id.phrase_dest_profil

        fun getLayoutId() : Int {
            return R.layout.friendzone2_phrase_dest
        }

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param glide : Glide
         * @param characters : TableOfCharacters
         * @param p : Phrase
         * @param itemView : View
         *
         * @see GameConversationAdapter
         */
        fun design(context: Context, glide: RequestManager, characters: TableOfCharacters, p: Phrase, itemView: View, isNightMode : Boolean, isPhoneMode : Boolean) {

            val character = characters.getCharacter(p.id_author)
            val text = itemView.findViewById<TextView>(PhraseDest.textViewId)
            val background = itemView.findViewById<FrameLayout>(PhraseDest.backgroundId)
            val gd = background.background as GradientDrawable

            text.text = p.sentence



            val image = itemView.findViewById<ImageView>(PhraseDest.imageId)

            if(isPhoneMode) {
                gd.setColor(ContextCompat.getColor(context, R.color.friendzone2_phone_background_dest))
                text.setTextColor(ContextCompat.getColor(context, R.color.friendzone2_phone_text_dest))
                return
            }

            text.setTextColor(ContextCompat.getColor(context, if(isNightMode) R.color.phrase_dest_night_mode_text else character.textColorId))
            glide.load(character.smallImageId).into(image)
            gd.setColor(ContextCompat.getColor(context, if(isNightMode) R.color.phrase_dest_night_mode_background else character.colorId))
        }
    }
}