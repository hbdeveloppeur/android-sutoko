package com.purpletear.smsgame.activities.smsgameloader

import android.content.Intent
import com.example.sharedelements.Data
import com.example.sharedelements.SutokoSharedElementsData
import com.purpletear.smsgame.activities.smsgame.SmsGameActivity
import com.purpletear.smsgame.activities.smsgame.objects.StoryChapter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import fr.purpletear.sutoko.shop.premium.Premium
import fr.purpletear.sutoko.shop.shop.SkuValidator
import purpletear.fr.purpleteartools.TableOfSymbols

class SmsGameLoaderModel(activity: SmsGameLoaderActivity) {
    var userHistoryHasStoryOrders: Boolean = activity.intent.getBooleanExtra(
        Data.Companion.Extra.USER_HISTORY_HAS_AT_LEAST_ONE_ORDER.id,
        false
    )
    val card: Game
    var symbols: TableOfSymbols
    var chapters: ArrayList<StoryChapter>

    init {
        card = (activity.intent.getParcelableExtra(Data.Companion.Extra.ITEM.id)
            ?: throw IllegalStateException())
        symbols =
            activity.intent.getParcelableExtra(Data.Companion.Extra.TABLE_OF_SYMBOLS.id)!!
        chapters =
            activity.intent.getParcelableArrayListExtra(Data.Companion.Extra.CHAPTERS_ARRAY.id)!!
    }

    fun shouldWatchAd(activity: SmsGameLoaderActivity): Boolean {
        if (card.isPremium()) {
            return false
        }
        
        return !((this.symbols.chapterNumber > 1 && Premium.userIsPremium(activity))
                || SkuValidator.userBoughtAStory(activity)
                || userHistoryHasStoryOrders
                || SkuValidator.userHasSku(activity, SutokoSharedElementsData.REMOVE_ADS_SKU))
    }

    fun updateWatchAdTime() {
        SutokoSharedElementsData.lastSeenAdsTime = System.currentTimeMillis()
    }

    /**
     * The Premium Activity is launched only if the story requires premium grant and the user already played chapter one
     * @param activity SmsGameLoaderActivity
     * @return Boolean
     */
    fun shouldStartPremiumActivity(activity: SmsGameLoaderActivity): Boolean {
        val userCanPlay = Premium.userIsPremium(activity) || SkuValidator.userHasSku(
            activity,
            this.card.skuIdentifiers
        ) || activity.intent.getBooleanExtra("isPaid", false)
        return this.gameIsPremium() && this.symbols.chapterNumber > 1 && !userCanPlay
    }

    private fun gameIsPremium(): Boolean {
        return card.isPremium()
    }

    fun nextChapterIsPlayable(): Boolean {
        val chapter = this.getCurrentChapter() ?: return false
        var res = true

        if (chapter.visibility == 0 && SutokoSharedElementsData.isDebugMode) {
            res = true
        } else if (chapter.visibility != 1) {
            res = false
        }

        if (!chapter.isCompatible() || chapter.requiresStoryUpdate(symbols) || !chapter.isAvailable() || chapter.userHasAccess == false) {
            res = false
        }
        return res
    }

    private fun getCurrentChapter(): StoryChapter? {
        this.chapters.forEach { chapter ->
            if (chapter.chapterCode == this.symbols.chapterCode) {
                return chapter
            }
        }
        return null
    }

    fun getSmsGameIntent(activity: SmsGameLoaderActivity): Intent {
        val intent = Intent(activity, SmsGameActivity::class.java)
        return SmsGameActivity.require(
            intent,
            activity.intent.getParcelableExtra(Data.Companion.Extra.ITEM.id) ?: card,
            this.chapters,
            StoryType.OFFICIAL_STORY
        )
    }
}