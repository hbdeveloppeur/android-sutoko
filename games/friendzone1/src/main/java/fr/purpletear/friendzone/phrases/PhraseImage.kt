/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.phrases

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.Phrase
import fr.purpletear.friendzone.tables.TableOfCharacters
import purpletear.fr.purpleteartools.Std

/**
 * Represents a Phrase Dest
 */
class PhraseImage {

    /**
     * Items id
     */
    companion object {
        var layoutId: Int = R.layout.friendzone1_phrase_image

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see GameConversationAdapter
         */
        fun design(context: Context, glide : RequestManager, characters : TableOfCharacters, p: Phrase, itemView: View) {
            val pers = characters.getCharacter(p.id_author)
            val image = itemView.findViewById<ImageView>(R.id.phrase_image_image)
            val profil = itemView.findViewById<ImageView>(R.id.phrase_image_profil)

            glide
                    .load(pers.smallImageId)
                    .into(profil)

            val id = Std.getResourceIdFromName(context, p.contentImageName, "drawable", -1)
            glide.load(id).into(image)
        }
    }
}