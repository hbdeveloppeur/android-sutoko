package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.view.View
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sutokosharedelements.GraphicsPreference
import com.example.sutokosharedelements.SmsGameTreeStructure
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Phrase

class PhraseDestImage {
    companion object {
        val LAYOUT_ID = R.layout.sutoko_phrase_dest_image

        enum class Views(val id: Int) {
            IMAGE(R.id.sutoko_phrase_dest_image_image),
        }

        fun design(
            itemView: View,
            context: Context,
            storyId: Int,
            requestManager: RequestManager,
            phrase: Phrase
        ) {
            val imagePath: String =
                SmsGameTreeStructure.getMediaFilePath(context, storyId, phrase.sentence)
            requestManager.load(imagePath)
                .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.IMAGE.id))
        }
    }
}