package com.purpletear.smsgame.activities.smsgame.items

import android.view.View
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sutokosharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import purpletear.fr.purpleteartools.FingerV2

object PhraseMangaPage {
    val LAYOUT_ID = R.layout.sutoko_item_manga_page

    fun design(itemView: View, requestManager: RequestManager) {
        requestManager.load(R.drawable.page_manga_preview)
            .apply(
                GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE)
                    .circleCrop()
            )
            .transition(withCrossFade())
            .into(itemView.findViewById(R.id.sutoko_item_manga_image))
    }

    fun setListener(itemView: View, onTouch: () -> Unit) {

        FingerV2.register(itemView, R.id.sutoko_item_manga_button_open) {
            onTouch()
        }

    }
}