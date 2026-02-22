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
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.main.MainInterface
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfCharacters
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Std

/**
 * Represents a Phrase Dest
 */
class PhraseVocal {

    /**
     * Itms id
     */
    companion object {
        var backgroundId: Int = R.id.phrase_vocal_background
        var button: Int = R.id.phrase_vocal_button
        var profil: Int = R.id.phrase_vocal_profil

        fun getLayoutId() : Int {
            return R.layout.phrase_vocal
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
        fun design(context: Context, glide: RequestManager, characters: TableOfCharacters, p: Phrase, itemView: View, playingSound : String) {

            val character = characters.getCharacter(p.id_author)
            val background = itemView.findViewById<FrameLayout>(backgroundId)
            val gd = background.background as GradientDrawable
            val image = itemView.findViewById<ImageView>(profil)
            val icon = itemView.findViewById<ImageView>(button)

            if(playingSound == OnlineAssetsManager.getSoundFilePath(context, GlobalData.Game.FRIENDZONE2.id.toString(),  "zv/${p.soundName}")) {
                glide.load(OnlineAssetsManager.getImageFilePath(context, GlobalData.Game.FRIENDZONE2.id.toString(), "vocal_sound_pause")).into(icon)
            } else {
                glide.load(OnlineAssetsManager.getImageFilePath(context, GlobalData.Game.FRIENDZONE2.id.toString(), "vocal_sound_play")).into(icon)
            }

            gd.setColor(ContextCompat.getColor(context, character.colorId))
            glide.load(character.smallImageId).into(image)
        }

        fun listener(context : Context, view : View, phrase :  Phrase, callback : MainInterface) {
            Finger.defineOnTouch(
                    view.findViewById(backgroundId),
                    context) {
                callback.onClickSound("zv/${phrase.soundName}")
            }
        }
    }
}