package com.purpletear.smsgame.activities.smsgame.items

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.bumptech.glide.RequestManager
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.ConversationInterface
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.FingerV2
import purpletear.fr.purpleteartools.Std

object PhraseRateDecoration {

    fun design(
        itemView: View,
        requestManager: RequestManager,
        card: Game,
        callback: ConversationInterface,
        phrase: Phrase
    ) {
        // TODO
//        requestManager
//            .load(Data.FIREBASE_STORAGE_URL_PREFIX + card.squareImagePrefix)
//            .transition(withCrossFade())
//            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
//            .into(itemView.findViewById(R.id.sutoko_item_rate_game_info_game_image))

        requestManager
            .load(R.drawable.ic_arrow_left_header_smsgame)
            .into(itemView.findViewById(R.id.sutoko_item_rate_game_button_back_image))

        FingerV2.register(
            itemView,
            R.id.sutoko_item_rate_game_button_back,
            callback::onRateStoryButtonBackPressed
        )

        val starsParent = itemView.findViewById<LinearLayout>(R.id.sutoko_item_rate_game_stars)
        displayStars(starsParent, phrase.seen - 1)
        starsParent.children.forEachIndexed { index, view ->
            val starView = view.findViewById<ImageView>(R.id.sutoko_star_rate_ic_unchecked)
            val starViewChecked = view.findViewById<ImageView>(R.id.sutoko_star_rate_ic_checked)
            requestManager
                .load(R.drawable.ic_star_unchecked)
                .into(starView)
            requestManager
                .load(R.drawable.sutoko_ic_star_checked)
                .into(starViewChecked)
            FingerV2.register(view, null) {
                Std.vibrate(itemView)
                displayStars(starsParent, index)
                callback.onRateStory(index + 1)
            }
        }
    }

    private fun displayStars(parent: LinearLayout, untilIndex: Int) {
        parent.children.forEachIndexed { index, view ->
            val starViewChecked = view.findViewById<ImageView>(R.id.sutoko_star_rate_ic_checked)
            if (untilIndex >= index) {
                starViewChecked.visibility = View.VISIBLE
            } else {
                starViewChecked.visibility = View.INVISIBLE
            }
        }
    }
}