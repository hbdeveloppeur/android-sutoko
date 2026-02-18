package com.purpletear.smsgame.activities.smsgame.items

import android.view.View
import android.widget.TextView
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Phrase

class PhraseInfo {
    companion object {
        enum class Views(val id: Int) {
            TEXT(R.id.sutoko_phrase_info_text)
        }

        /**
         * Designs the phrase
         *
         * @param itemView
         * @param phrase
         */
        fun design(itemView: View, phrase: Phrase) {
            itemView.findViewById<TextView>(Views.TEXT.id).text = phrase.sentence
        }
    }
}