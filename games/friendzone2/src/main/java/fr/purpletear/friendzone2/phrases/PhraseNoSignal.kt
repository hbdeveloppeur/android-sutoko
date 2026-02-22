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
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import purpletear.fr.purpleteartools.GlobalData

/**
 * Represents a Phrase Dest
 */
class PhraseNoSignal {

    /**
     * Items id
     */
    companion object {
        var layoutId: Int = R.layout.phrase_no_signal
        var textViewId: Int = R.id.phrase_nosignal_text

        /**
         * Designs a Phrase Dest
         * @param context : Context
         * @param p : Phrase
         * @param itemView : View
         *
         * @see fr.purpletear.friendzone.activities.main.GameConversationAdapter
         */
        fun design(context: Context, p: Phrase, itemView: View, requestManager: RequestManager) {
            val text = itemView.findViewById<TextView>(textViewId)
            text.setTextColor(ContextCompat.getColor(context, R.color.white))
            requestManager.load(
                OnlineAssetsManager.getImageFilePath(context, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_no_signal")
            ).into(itemView.findViewById(R.id.phrase_nosignal_image))
        }
    }
}