package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.SutokoSharedElementsData
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.ConversationInterface
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.FingerV2

object PhraseNextChapterDecoration {
    val LAYOUT_ID = R.layout.sutoko_item_nextchapter

    private fun canWatchDiamondsAds(): Boolean {
        val lastTime = SutokoSharedElementsData.lastSeenDiamondAdsTime ?: return true
        val difference = (System.currentTimeMillis() - lastTime)

        @Suppress("UnnecessaryVariable")
        val isAtLeastOneHourOld = difference > 30 * 60 * 1000
        return isAtLeastOneHourOld
    }

    fun design(
        context: Context,
        itemView: View,
        requestManager: RequestManager,
        callback: ConversationInterface,
        card: Game,
        chapterNumber: Int
    ) {
        val diamonds = 30
        FingerV2.register(itemView, R.id.sutoko_smsgame_nextchapter_button_play) {
            callback.onNextChapterPressed()
        }
        FingerV2.register(itemView, R.id.sutoko_smsgame_nextchapter_button_diamonds) {
            SutokoSharedElementsData.lastSeenDiamondAdsTime = System.currentTimeMillis()
            callback.onWatchAdsForDiamonds(diamonds)
        }
        itemView.findViewById<TextView>(R.id.sutoko_item_nextchapter_unlocked_text).text =
            context.getString(
                R.string.sutoko_smsgame_item_nextchapter_chapter_unlocked,
                chapterNumber
            )

        requestManager.load(R.drawable.ic_et_lock)
            .into(itemView.findViewById(R.id.sutoko_item_nextchapter_unlocked_icon))

        val buttonNextChapter = mapOf(
            // TODO
            // R.id.sutoko_item_nextchapter_button_play_image_story_logo to Data.FIREBASE_STORAGE_URL_PREFIX + card.squareImagePrefix,
            R.id.sutoko_item_nextchapter_button_play_image_background to R.drawable.sutoko_next_chapter_item_bg,
            R.id.sutoko_item_nextchapter_button_play_image_foreground to R.drawable.sutoko_next_chapter_item_foreground2,
            R.id.sutoko_item_nextchapter_icon_arrow to R.drawable.sutoko_ic_arrow_left_long
        )
        val buttonNextChapterView =
            itemView.findViewById<ViewGroup>(R.id.sutoko_smsgame_nextchapter_button_play)
        buttonNextChapter.forEach { (viewId, drawableId) ->
            requestManager.load(drawableId).into(buttonNextChapterView.findViewById(viewId))
        }
        val buttonDiamondsView =
            itemView.findViewById<ViewGroup>(R.id.sutoko_smsgame_nextchapter_button_diamonds)
        buttonDiamondsView.findViewById<LottieAnimationView>(R.id.sutoko_item_nextchapter_button_play_animation).visibility =
            View.VISIBLE
        buttonDiamondsView.alpha = 0.6f
        if (canWatchDiamondsAds()) {
            buttonDiamondsView.visibility = View.VISIBLE
            val buttonDiamonds = mapOf(
                R.id.sutoko_item_nextchapter_button_play_image_background to R.drawable.button_see_ads_bg_next_chapter,
                R.id.sutoko_item_nextchapter_icon_arrow to R.drawable.sutoko_ic_arrow_left_long
            )
            buttonDiamonds.forEach { (viewId, drawableId) ->
                requestManager.load(drawableId).into(buttonDiamondsView.findViewById(viewId))
            }
            buttonDiamondsView.findViewById<TextView>(R.id.sutoko_item_nextchapter_cta_text).text =
                context.getString(R.string.sutoko_unlock_diamonds, diamonds)
        } else {
            buttonDiamondsView.visibility = View.GONE
        }
        buttonNextChapterView.findViewById<TextView>(R.id.sutoko_item_nextchapter_cta_text).text =
            context.getString(R.string.sutoko_smsgame_item_nextchapter_button_play_text)
    }
}