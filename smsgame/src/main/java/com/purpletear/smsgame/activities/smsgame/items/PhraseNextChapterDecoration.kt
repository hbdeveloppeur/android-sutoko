package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.ConversationInterface
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.FingerV2

object PhraseNextChapterDecoration {
    val LAYOUT_ID = R.layout.sutoko_item_nextchapter

    fun design(
        context: Context,
        itemView: View,
        requestManager: RequestManager,
        callback: ConversationInterface,
        card: Game,
        chapterNumber: Int
    ) {
        FingerV2.register(itemView, R.id.sutoko_smsgame_nextchapter_button_play) {
            callback.onNextChapterPressed()
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

        // REMOVED: Watch ad for diamonds button - ads no longer supported
        val buttonDiamondsView =
            itemView.findViewById<ViewGroup>(R.id.sutoko_smsgame_nextchapter_button_diamonds)
        buttonDiamondsView.visibility = View.GONE

        buttonNextChapterView.findViewById<TextView>(R.id.sutoko_item_nextchapter_cta_text).text =
            context.getString(R.string.sutoko_smsgame_item_nextchapter_button_play_text)
    }
}
