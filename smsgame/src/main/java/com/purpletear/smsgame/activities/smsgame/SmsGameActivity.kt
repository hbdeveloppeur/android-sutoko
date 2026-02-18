package com.purpletear.smsgame.activities.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.DarkModeHelper
import com.example.sharedelements.Data
import com.example.sharedelements.SmsGameTreeStructure
import com.example.sharedelements.SutokoSharedElementsData
import com.google.firebase.analytics.FirebaseAnalytics
import com.purpletear.smartads.SmartAdsInterface
import com.purpletear.smartads.adConsent.AdmobConsent
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.manga.MangaPageActivity
import com.purpletear.smsgame.activities.previewVisualMedia.PreviewVisualMedias
import com.purpletear.smsgame.activities.smsgame.adapter.SmsGamePhraseVocalListener
import com.purpletear.smsgame.activities.smsgame.items.PhraseDest
import com.purpletear.smsgame.activities.smsgame.objects.ChoiceAction
import com.purpletear.smsgame.activities.smsgame.objects.ChoiceControllerInterface
import com.purpletear.smsgame.activities.smsgame.objects.ChoicesController
import com.purpletear.smsgame.activities.smsgame.objects.CreatorResource
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.SmsNotification
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.smsgame.activities.smsgame.objects.StoryChapter
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.objects.StoryHelper
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.smsgame.activities.smsgamevideointroduction.SmsGameVideoIntro
import com.purpletear.smsgame.databinding.ActivitySmsGameBinding
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.purpletear.sutoko.shop.coinsLogic.CustomerCallbacks
import purpletear.fr.purpleteartools.FingerV2
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.SimpleVideo
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import java.io.File
import java.io.FileNotFoundException
import java.io.Serializable


class SmsGameActivity : AppCompatActivity(), ConversationInterface, ChoiceControllerInterface,
    SmsGamePhraseVocalListener, SmartAdsInterface, CustomerCallbacks {
    lateinit var binding: ActivitySmsGameBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var model: SmsGameModel
    private lateinit var graphics: SmsGameGraphics
    private var shouldProvokNextOnFirstStart: Boolean = true
    private lateinit var mangaActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var introActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var adsActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsGameBinding.inflate(layoutInflater)
        SutokoSharedElementsData.setStrictMode()

        this.mangaActivityResultLauncher = this.registerMangaActivityResultLauncher()
        this.introActivityResultLauncher = this.registerIntroActivityResultLauncher()
        this.adsActivityResultLauncher = AdmobConsent.registerActivityResultLauncher(this, this)

        setContentView(binding.root)
        Std.hideStatusBar(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        try {

            model = SmsGameModel(this)
        } catch (e: FileNotFoundException) {

            val lang = Language.determineLangDirectory()
            val code = SmsGameModel.chapterCode
            val version = SmsGameModel.version
            val storyId = SmsGameModel.storyId
            this.setResult(1003)
            this.finish()
            return
        }
        graphics = SmsGameGraphics()
        SmsGameGraphics.setRecyclerView(this, model.conversation.adapter)
        setListeners()
        logEvent()
        model.insertGameInfoHeaderIfRequired(this, {
            // onLikeButtonPressed
            if (StoryHelper.userLiked(this.model.story!!.id)) {
                SmsGameGraphics.unlinke(this)
                model.unlike(this)
            } else {
                SmsGameGraphics.like(this)
                model.like(this)
            }
        }, {
            model.openAuthorProfile(this)
        })
        designDarkMode()
        SmsGameGraphics.setFakeStatusBarSize(this)
    }

    override fun onBackPressed() {
        if (SmsGameGraphics.unlockItemIsVisible(this)) {
            SmsGameGraphics.setUnlockItemIsVisible(this, false)
            return
        }
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onPause() {
        if (::model.isInitialized) {
            model.tableOfPlayer.onPause()
            model.videoPlayer?.onPause()
        }
        super.onPause()
    }

    override fun onStop() {
        if (::model.isInitialized) {
            model.tableOfPlayer.onStop()
            model.videoPlayer?.onStop()
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (::model.isInitialized) {
            this.model.delayHandler?.stop()
            this.model.delayHandler = null
        }
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            if (shouldProvokNextOnFirstStart) {
                model.conversation.provokeNext(model.symbols, false)
            }
            model.notifyRead(this)

            SmsGameGraphics.fadeFilter(this, false, 1280)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("card", model.card as Parcelable)
        outState.putParcelableArrayList("phrases", model.conversation.adapter.array)
        outState.putParcelable("currentPhrase", model.conversation.currentPhrase)
        outState.putParcelable("symbols", model.symbols)
        outState.putBoolean("waitForUserInput", model.waitForUserInput)
        outState.putString("waitingInputCode", model.waitingInputCode)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.conversation.adapter.array =
            savedInstanceState.getParcelableArrayList("phrases") ?: ArrayList()
        model.conversation.currentPhrase = savedInstanceState.getParcelable("currentPhrase")
        model.card = savedInstanceState.getParcelable("card")
            ?: throw RuntimeException("Card is null")
        model.symbols = savedInstanceState.getParcelable<TableOfSymbols>("symbols")
            ?: TableOfSymbols(model.card.id)
        shouldProvokNextOnFirstStart = false
        model.waitForUserInput = savedInstanceState.getBoolean("waitForUserInput")
        model.waitingInputCode = savedInstanceState.getString("waitingInputCode")
    }

    override fun onCoinsOrDiamondsUpdated(coins: Int, diamonds: Int) {

    }

    private fun designDarkMode() {
        val isDarkMode = DarkModeHelper.isDarkMode(this)
        SmsGameGraphics.setDarkModeTheme(this, this.model.requestManager, isDarkMode)
    }

    private fun onDarkModeEnabled() {
        val isDarkMode = !DarkModeHelper.isDarkMode(this)
        SmsGameGraphics.setDarkModeTheme(this, this.model.requestManager, isDarkMode)
        DarkModeHelper.setDarkMode(this, isDarkMode)
    }

    override fun onNextChapterPressed() {
        setResult(Activity.RESULT_OK)
        this.model.goToNextChapter(this@SmsGameActivity)
    }

    override fun onWatchAdsForDiamonds(id: Int) {
        AdmobConsent.start(this, this.adsActivityResultLauncher)
    }

    override fun onUserInputRequestFound(symboleName: String) {
        model.waitingInputCode = symboleName
        model.waitForUserInput = true
        model.conversation.adapter.insert(Phrase(Phrase.Type.makeChoice))
        onPhraseInserted()
    }

    override fun onFilterFadeOut(delay: Int, duration: Int) {

    }

    override fun onFilterFadeIn(delay: Int, duration: Int) {

    }

    override fun onVocalMessageFound(phrase: Phrase) {
        val path = SmsGameTreeStructure.getMediaFilePath(
            this,
            model.card.id,
            phrase.sentence.replace(" ", "")
        )
        model.tableOfPlayer.addToPreloadList(path)
        model.tableOfPlayer.preload(this)
        val isMainCharacter =
            this.model.conversation.characters.getCharacter(phrase.id_author).isMainCharacter
        phrase.setType(if (isMainCharacter) Phrase.Type.vocalMe else Phrase.Type.vocal)
        model.conversation.insert(phrase)
    }

    override fun onPressed(phrase: Phrase, position: Int) {
        val path = SmsGameTreeStructure.getMediaFilePath(
            this,
            model.card.id,
            phrase.sentence.replace(" ", "")
        )
        if (model.tableOfPlayer.isPlaying(path)) {
            model.conversation.adapter.setIsPlaying(phrase, false)
            model.tableOfPlayer.pause(path)
            model.tableOfPlayer.rollback(path)
        } else {
            model.conversation.adapter.setIsPlaying(phrase, true)
            model.tableOfPlayer.play(path, onStart@{

            }, onFinish@{
                model.conversation.adapter.setIsPlaying(phrase, false)
                model.tableOfPlayer.pause(path)
                model.tableOfPlayer.rollback(path)
                model.conversation.adapter.notifyItemChanged(position)
            }, false)
        }

        val duration = model.tableOfPlayer.duration(path)
        model.conversation.adapter.array[position].seen = duration.toInt()
        model.conversation.adapter.notifyItemChanged(position)

    }

    override fun onSoundFound(
        soundNameWithExtension: String,
        channel: Int,
        isLooping: Boolean,
        delay: Int
    ) {
        this.model.delayHandler?.operation("onSoundFound", delay) {
            val path =
                SmsGameTreeStructure.getMediaFilePath(this, model.card.id, soundNameWithExtension)
            model.tableOfPlayer.addToPreloadList(path)
            model.tableOfPlayer.preload(this)
            model.tableOfPlayer.play(path, onStart@{

            }, onFinish@{
                if (!isFinishing && isLooping) {
                    onSoundFound(soundNameWithExtension, channel, isLooping, delay)
                }
            }, !isLooping)
        }
    }

    override fun onNextChapterFound() {
        this.model.symbols.addChapterToRoute(this.model.symbols.chapterCode)
        this.model.save(this)

        // Do not need to display "go to next chapter"
        var isChapterCorretion = false
        try {

            isChapterCorretion =
                StoryChapter.numberFromCode(code = this.model.symbols.chapterCode) == StoryChapter.numberFromCode(
                    SmsGameModel.chapterCode
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!isChapterCorretion) {
            this.model.insertChapterSwitchCell(this)
            onPhraseInserted()
        } else {
            this.onNextChapterPressed()
        }
    }

    override fun onRateStoryButtonBackPressed() {
        finish()
    }

    override fun onActionChoicePressed(phraseId: Int) {
        model.conversation.adapter.removeLastIf(Phrase.Type.actionChoice)
        model.conversation.startFrom(phraseId, model.symbols)
    }

    private fun registerMangaActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            model.conversation.continueAfterMangaPage(model.symbols)
        }
    }

    private fun registerIntroActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                this.model.conversation.startFrom(
                    this.model.conversation.currentPhrase!!.id,
                    model.symbols
                )
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }


    override fun onMangaPageButtonPressed(filename: String) {
        this.mangaActivityResultLauncher.launch(
            MangaPageActivity.require(
                this,
                filename,
                model.card.id,
                model.conversation.getMangaMessages(model.symbols)
            )
        )
    }


    override fun onNarratorSentenceFound(
        sentence: String,
        mode: Phrase.NarratorSentenceMode,
        delay: Int,
        duration: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onHeaderUpdateFound(
        imageNameWithExtension: String,
        title: String,
        subTitle: String,
        type: Phrase.PhraseUpdateHeaderImageType,
        delay: Int
    ) {

    }

    override fun onHeaderGraphicsUpdateFound(
        colorHexaCode: String,
        colorAlphaPercent: Int,
        isIconVisible: Boolean,
        delay: Int
    ) {

    }

    override fun onSimpleMessageFound(phrase: Phrase) {

        val shouldDisplayTypingAnimation: Boolean =
            model.symbols.condition(model.card.id, "typingAnimation", "true")

        if (shouldDisplayTypingAnimation && !model.isMainCharacter(phrase)) {
            this.model.playTypingSound(this)
            model.conversation.currentPhrase = phrase
            val p = Phrase(Phrase.Type.isTyping)
            p.id = phrase.id
            p.id_author = phrase.id_author
            model.conversation.insert(p)
        }

        model.operation(
            this,
            "typing:a",
            if (shouldDisplayTypingAnimation) model.determinePhraseTypingDuration(phrase) else 0
        ) {
            insertOrEditPhrase(
                phrase,
                !(shouldDisplayTypingAnimation && !model.isMainCharacter(phrase))
            ) {
                // if isMaincharacter && is typing.
                val nextMessagesLinks = model.conversation.links.getDest(phrase.id)
                val hasNextMessageWithSameAuthor =
                    nextMessagesLinks.size == 1 && model.conversation.phrases.getPhrases(
                        nextMessagesLinks
                    )[0].id_author == phrase.id_author


                if (model.isMainCharacter(phrase) && shouldDisplayTypingAnimation && !hasNextMessageWithSameAuthor) {
                    // set sent.
                    this.model.conversation.adapter.arrayOfSeenMessages[phrase.id] =
                        PhraseDest.Companion.SeenState.SENT
                    // Remove replace last and change with replace by phrase id
                    this.model.conversation.adapter.replaceItemByPhraseId(phrase)
                    // Update
                    this.model.operation(this, "typing:b:${phrase.id}", 1280) {
                        this.model.conversation.adapter.arrayOfSeenMessages[phrase.id] =
                            PhraseDest.Companion.SeenState.SEEN
                        // Remove replace last and change with replace by phrase id
                        this.model.conversation.adapter.replaceItemByPhraseId(phrase)
                    }
                }
            }
        }
    }

    override fun onPhraseInfoFound(phrase: Phrase) {
        insertOrEditPhrase(phrase, true)
    }

    private fun insertOrEditPhrase(phrase: Phrase, isInsert: Boolean, onInserted: () -> Unit = {}) {

        val isMainCharacter = if (phrase.id_author == -1) {
            false
        } else {
            model.conversation.characters.getCharacter(phrase.id_author).isMainCharacter
        }
        if (isMainCharacter) {
            phrase.setType(Phrase.Type.meExtended)
        }

        val delay = if (model.conversation.isAutomaticTimeMode()) {
            phrase.seen
        } else {
            0
        }

        model.operation(this, "onSimpleMessageFound", delay) {
            if (isInsert) {
                model.conversation.insert(phrase)
            } else {
                model.conversation.adapter.replaceItemByPhraseId(phrase)
            }

            onInserted()

            if (model.conversation.isAutomaticTimeMode()) {
                model.conversation.provokeNext(model.symbols)
            }
            model.conversation.controlUserChoice()
        }
    }

    override fun onRateStory(mark: Int) {
        this.model.rateStory(this, mark)
    }

    override fun onImageMessageFound(imageNameWithExtension: String, characterId: Int, delay: Int) {
        val isMainCharacter =
            model.conversation.characters.getCharacter(characterId).isMainCharacter
        val phrase = Phrase(Phrase.Type.image)
        phrase.sentence = imageNameWithExtension
        if (isMainCharacter) {
            phrase.setType(Phrase.Type.imageMe)
        }

        val sdelay = if (model.conversation.isAutomaticTimeMode()) {
            delay
        } else {
            0
        }

        model.operation(this, "onImageMessageFound", sdelay) {
            model.conversation.insert(phrase)
            if (model.conversation.isAutomaticTimeMode()) {
                model.conversation.provokeNext(model.symbols)
            }
        }
    }

    override fun onBackgroundVideoFound(videoNameWithExtension: String, delay: Int) {
        switchMediaTo(SmsGameModel.MediaType.VIDEO, SmsGameModel.ViewType.VIDEOVIEW)
        SimpleVideo.start(
            this,
            R.id.smsgame_videoview_old,
            File(
                SmsGameTreeStructure.getMediaFilePath(
                    this,
                    model.card.id,
                    videoNameWithExtension
                )
            ),
            true
        )
    }

    override fun onBackgroundImageFound(imageNameWithExtension: String, delay: Int) {
        val curtainsAreOpened = SmsGameGraphics.curtainsAreOpen(this)
        val duration = if (curtainsAreOpened) {
            SmsGameGraphics.fadeCurtains(this, true)
        } else {
            0
        }
        this.model.delayHandler?.operation("onBackgroundImageFound", duration.toInt()) {
            SmsGameGraphics.setBackgroundImage(
                this,
                SmsGameTreeStructure.getMediaFilePath(
                    this,
                    this.model.card.id,
                    imageNameWithExtension
                ),
                model.requestManager,
                onLoaded@{
                    switchMediaTo(SmsGameModel.MediaType.IMAGE, SmsGameModel.ViewType.NONE)
                }, onFailure@{

                }
            )
        }
    }

    override fun onBackgroundColorFound(colorHexaCode: String, delay: Int) {

    }

    override fun onElementVisibilityUpdateFound(
        element: Phrase.UpdateElementVisibilityType,
        visibilityValue: Phrase.VisibilityValue,
        delay: Int,
        duration: Int
    ) {

    }

    override fun onPhoneVibrateFound(numberOfVibrations: Int, delay: Int) {

    }

    override fun onHesitatingCharacterFound(characterId: Int, delay: Int, duration: Int) {
        this.model.delayHandler?.operation("hesitation:a", delay) {
            // display

            val p = Phrase(Phrase.Type.isTyping)
            p.id_author = characterId
            model.conversation.insert(p)
            this.model.playTypingSound(this)

            this.model.delayHandler?.operation("hesitation:b", duration) {
                // Disappear
                if (model.conversation.adapter.lastHasType(Phrase.Type.isTyping)) {
                    val lastIndex: Int = model.conversation.adapter.array.lastIndex
                    model.conversation.adapter.array.removeAt(lastIndex)
                    model.conversation.adapter.notifyItemRemoved(lastIndex)
                    model.conversation.provokeNext(model.symbols)
                }
            }
        }
    }

    private fun onCoinsDiscovered(phraseId: Int, code: String, amount: Int) {
        if (this.model.customer.history.hasGain(code)) {
            return
        }
        val str = this.getString(R.string.sutoko_story_unlock_coins, amount)
        val p = Phrase(Phrase.Type.info)
        p.sentence = str
        p.id = phraseId
        insertOrEditPhrase(p, true) {

        }
        this.model.customer.onUnlockedCoins(
            this,
            code,
            amount
        ) { isSuccessful, exception ->
            if (isFinishing || this.model.conversation.isFinished) {
                return@onUnlockedCoins
            }
        }
    }

    override fun onChoiceActionsFound(actions: ArrayList<ChoiceAction>) {
        model.conversation.adapter.currentActionChoices = actions
        val phrase = Phrase(Phrase.Type.actionChoice)
        model.conversation.adapter.insert(phrase)
        onPhraseInserted()
    }

    override fun onMemoryToSaveFound(
        phraseId: Int,
        name: String,
        value: String,
        chapterNumber: Int,
        coins: Int
    ) {

        if (name == "realTime" && value == "false") {
            // SmsGameNotificationHelper.cancelNotification(this, model.card.id)
        }
        if (name == "realTime" && value == "true") {
            val p = Phrase(Phrase.Type.info)
//            if (!SmsGameNotificationHelper.notificationEnabled(this)) {
//                p.sentence = getString(R.string.sutoko_sms_game_real_time_activated_alert)
//            } else {
            p.sentence = getString(R.string.sutoko_sms_game_real_time_activated)
            //}
            model.conversation.insert(p)
        }

        if (coins > 0) {
            this.onCoinsDiscovered(phraseId, "${name}_$value", coins)
        }

        model.symbols.addOrSet(model.card.id, name, value)
    }

    override fun onIntroFound(id: Int) {
        // Fade filter in
        val phrases = ArrayList<Phrase>()
        val links = ArrayList<TableOfLinks.Link>()
        getIntroPhrases(model.conversation.phrases.getPhrase(id), phrases, links)
        val intent = Intent(this, SmsGameVideoIntro::class.java)
        intent.putParcelableArrayListExtra(Data.Companion.Extra.LINKS.id, links)
        intent.putParcelableArrayListExtra(Data.Companion.Extra.PHRASES.id, phrases)
        intent.putExtra(Data.Companion.Extra.STORY_ID.id, this.model.card.id)
        this.introActivityResultLauncher.launch(intent)
    }

    private fun getIntroPhrases(
        phrase: Phrase,
        phrases: ArrayList<Phrase>,
        links: ArrayList<TableOfLinks.Link>
    ): Phrase? {
        val l = model.conversation.links.getDest(phrase.id)
        if (l.size == 0) {
            return null
        }
        var next = model.conversation.phrases.getPhrase(l[0])

        if (next.`is`(Phrase.Type.memory)) {
            val v = next.getVarFromCondition("1a")
            if (v.name == "intro" && v.value == "end") {
                this.model.conversation.currentPhrase = next
            }
        }

        var link = TableOfLinks.Link("${phrase.id}", "${l[0]}", 0)
        if (next.`is`(Phrase.Type.condition)) {
            next =
                model.conversation.determineConditionResult(model.symbols, next.answerCondition)!!
            link = TableOfLinks.Link("${phrase.id}", "${next.id}", 0)
        }
        phrases.add(next)
        links.add(link)
        getIntroPhrases(next, phrases, links)
        return null
    }

    override fun onChoicesToMakeFound(array: ArrayList<Phrase>) {
        ChoicesController.clear(SmsGameGraphics.getChoiceBoxContentParent(this))
        val choiceBoxParent = SmsGameGraphics.getChoiceBoxContentParent(this)
        ChoicesController.fill(
            this,
            this.model.requestManager,
            this.model.card,
            this.model.customer.history,
            this,
            choiceBoxParent,
            array,
            model.symbols,
            DarkModeHelper.isDarkMode(this)
        )
        model.conversation.adapter.insert(Phrase(Phrase.Type.makeChoice))
        onPhraseInserted()
    }

    override fun onColorsUpdateFound(
        element: Phrase.ColorsUpdateElement,
        colorHexaCode: String,
        delay: Int
    ) {

    }

    override fun onFilterUpdateFound(colorHexaCode: String, colorAlphaPercent: Int, delay: Int) {

    }

    override fun onPhraseInserted() {
        SmsGameGraphics.scrollToPosition(this, model.conversation.lastIndex)
    }

    override fun onRegisterScoreFound(isWin: Boolean) {
        if (isWin) {
            //StoryPreviewRankHelper.registerWinOnline(this, model.symbols)
        } else {
            //StoryPreviewRankHelper.registerLoseOnline(this, model.symbols)
        }
    }

    override fun conversationFinished() {
        if (model.conversation.isFinished) {
            return
        }
        model.symbols.setHasFinishedStoryOnce()
        model.conversation.isFinished = true
        val bundle = Bundle()
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, model.card.id)
        bundle.putString("state", "onFinished")
        firebaseAnalytics.logEvent("SHORT_SMSGAME_ON_GAME_STATE", bundle)
        model.symbols.save(this)
        model.conversation.insert(Phrase(Phrase.Type.end))
        if (model.storyType == StoryType.OFFICIAL_STORY) {
            val rate = Phrase(Phrase.Type.rate)
            rate.seen = model.getMyCurrentStoryMark()
            model.conversation.insert(rate)
        }
    }

    override fun onNotificationFound(
        storyId: Int,
        character: StoryCharacter,
        textContent: String,
        inXDays: Int,
        atHour: Int,
        atMin: Int,
        code: String
    ) {
        /* TODO   val time = Conversation.notification(
             this,
             model.card,
             model.conversation.chapters,
             model.requestManager,
             character,
             textContent,
             inXDays,
             atHour,
             atMin
         )
         this.model.symbols.nextNotificationTimeInMs = time

         if (!this.model.symbols.willNotifyForAGame) {
             this.model.symbols.nextNotificationResult = "cancel"
         } else {
             this.model.symbols.nextNotificationResult = "ok"
         }
 */
        this.model.symbols.chapterCode = code
        this.model.symbols.save(this)
    }

    override fun onBackgroundVideoCreatorResourceFound(creatorResource: CreatorResource) {
        model.videoPlayer?.release()
        /*model.videoPlayer = PurpleExoPlayer(R.string.app_name, creatorResource.url, true)

        model.videoPlayer!!.create(this, binding.smsgameVideoview)
        model.videoPlayer!!.play(onIsPlayingChanged@{ isPlaying ->
            if (isPlaying) {
                switchMediaTo(SmsGameModel.MediaType.VIDEO, SmsGameModel.ViewType.PLAYERVIEW)
            }
        }, onEnded@{

        }) */
    }

    override fun onBackgroundImageCreatorResourceFound(creatorResource: CreatorResource) {
        val duration = SmsGameGraphics.fadeCurtains(this, true)
        this.model.delayHandler?.operation("updateBackground", duration.toInt()) {
            SmsGameGraphics.setBackgroundImage(
                this,
                creatorResource.url,
                model.requestManager,
                onLoaded@{
                    switchMediaTo(SmsGameModel.MediaType.IMAGE, SmsGameModel.ViewType.NONE)
                }, onFailure@{

                }
            )
        }

    }

    override fun onSoundCreatorResourceFound(creatorResource: CreatorResource) {
        model.tableOfPlayer.addToPreloadList(creatorResource.url)
        model.tableOfPlayer.preload(this)
        model.tableOfPlayer.play(creatorResource.url, onStart@{

        }, onFinish@{

        })
        /*model.delayHandler.operation("soundPlayed", 1280) {
            model.conversation.provokeNext(model.symbols)
        }*/
    }

    override fun onFakeNotificationFound(
        title: String,
        description: String,
        imageName: String,
        actionText: String,
        duration: Int
    ) {
        val smsNotification =
            SmsNotification(
                model.requestManager,
                title,
                description,
                actionText,
                SmsGameTreeStructure.getMediaFilePath(this, model.card.id, imageName)
            )
        smsNotification.attach(
            this,
            /* onAttached */
            {
                smsNotification.animate(this@SmsGameActivity, true, 0) {
                    smsNotification.animate(this, false, duration) {
                        smsNotification.detach(this)
                    }
                }
            },
            /* onTouch */
            {

            })
    }

    private fun onRecyclerViewFastTouched() {
        if (model.conversation.isFinished
            || model.conversation.adapter.lastHasType(Phrase.Type.mangaPagePreview)
            || model.conversation.adapter.lastHasType(Phrase.Type.actionChoice)
        ) {
            return
        }
        this.model.delayHandler?.stop("onClickChoice")
        this.model.delayHandler?.stop("typing:a")
        this.model.delayHandler?.stop("onSimpleMessageFound")
        if (model.conversation.adapter.lastHasType(Phrase.Type.isTyping) && model.conversation.currentPhrase != null) {
            insertOrEditPhrase(model.conversation.currentPhrase!!, false)
        } else if (model.conversation.isChoiceMode) {
            SmsGameGraphics.setChoiceBoxVisibility(this, true)
        } else if (model.waitForUserInput) {
            model.requestUserInput(this) { value ->
                this.model.waitForUserInput = false
                if (this.model.waitingInputCode == null) {
                    return@requestUserInput
                }
                this.model.addSymbols(this.model.waitingInputCode!!, value)
                model.conversation.adapter.removeLastIf(Phrase.Type.makeChoice)
                model.conversation.provokeNext(model.symbols, true)
            }
        } else {
            model.conversation.provokeNext(model.symbols, true)
        }
    }

    private fun setListeners() {
        FingerV2.registerTouchOnRecyclerView(binding.sutokoSmsgameRecyclerviewConversation, this) {
            onRecyclerViewFastTouched()
        }
        FingerV2.register(this, R.id.sutoko_choicebox_background_hitbox, ::onCloseChoiceBoxPressed)
        FingerV2.register(this, R.id.sutoko_smsgame_choicebox_button_darkmode, ::onDarkModeEnabled)
    }

    override fun onClickSecretChoice(
        phrase: Phrase,
        diamonds: Int,
        action: () -> Unit,
        onError: () -> Unit
    ) {
        this.model.customer.onBuyChoice(
            this,
            this.model.card.id,
            "${this.model.card.id}s${phrase.id}",
            diamonds
        ) { isSuccessful, exception ->
            if (exception != Customer.ResultCode.NOT_ENOUGH_DIAMONDS) {
                action()
            } else {
                onError()
                Toast.makeText(
                    applicationContext,
                    R.string.sutoko_not_enough_diamonds,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onClickChoice(phrase: Phrase) {
        if (!model.conversation.isChoiceMode) {
            return
        }
        model.conversation.isChoiceMode = false
        SmsGameGraphics.setChoiceBoxVisibility(this, false)
        model.conversation.adapter.removeLastIf(Phrase.Type.makeChoice)
        insertOrEditPhrase(phrase, true)
        model.conversation.currentPhrase = phrase


        // if isMaincharacter && is typing.
        val shouldDisplayTypingAnimation: Boolean =
            model.symbols.condition(model.card.id, "typingAnimation", "true")
        val nextMessagesLinks = model.conversation.links.getDest(phrase.id)
        val hasNextMessageWithSameAuthor =
            nextMessagesLinks.size == 1 && model.conversation.phrases.getPhrases(nextMessagesLinks)[0].id_author == phrase.id_author

        if (model.isMainCharacter(phrase) && shouldDisplayTypingAnimation && !hasNextMessageWithSameAuthor) {
            // set sent.
            this.model.conversation.adapter.arrayOfSeenMessages[phrase.id] =
                PhraseDest.Companion.SeenState.SENT
            // Remove replace last and change with replace by phrase id
            this.model.conversation.adapter.replaceItemByPhraseId(phrase)
            // Update
            this.model.operation(this, "typing:b:${phrase.id}", 1280) {
                this.model.conversation.adapter.arrayOfSeenMessages[phrase.id] =
                    PhraseDest.Companion.SeenState.SEEN
                // Remove replace last and change with replace by phrase id
                this.model.conversation.adapter.replaceItemByPhraseId(phrase)
            }
        }

        this.model.delayHandler?.operation("onClickChoice", 1280) {
            onRecyclerViewFastTouched()
        }
    }

    private fun onCloseChoiceBoxPressed() {
        SmsGameGraphics.setChoiceBoxVisibility(this, false)
    }

    override fun onProfilePictureTouched(imageView: ImageView, characterId: Int) {
        if (model.storyType == StoryType.CURRENT_USER_STORY || model.storyType == StoryType.OTHER_USER_STORY) {
            return
        }
        val a = ArrayList<String>()
        a.add(SmsGameTreeStructure.getCharactersPictureFilePath(this, model.card.id, characterId))

        val intent = Intent(this, PreviewVisualMedias::class.java)
        Handler(Looper.getMainLooper()).post {
            PreviewVisualMedias.startActivityWithDrawableString(this, intent, imageView, a, 0)
        }
    }

    private fun logEvent() {
        val bundle: Bundle = Bundle()
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, model.card.id)
        bundle.putString("state", "onStarted")
        firebaseAnalytics.logEvent("SHORT_SMSGAME_ON_GAME_STATE", bundle)
    }


    private fun switchMediaTo(mediaType: SmsGameModel.MediaType, viewType: SmsGameModel.ViewType) {
        if (model.currentMediaTypeDisplayed == mediaType) {
            SmsGameGraphics.fadeCurtains(this, false)
            return
        }
        if (mediaType == SmsGameModel.MediaType.NONE) {
            model.currentMediaTypeDisplayed = SmsGameModel.MediaType.NONE
            SmsGameGraphics.setBackgroundImageVisibility(this, false)
            SmsGameGraphics.setBackgroundVideoVisibility(
                this,
                false,
                SmsGameModel.ViewType.VIDEOVIEW
            )
            SmsGameGraphics.setBackgroundVideoVisibility(
                this,
                false,
                SmsGameModel.ViewType.PLAYERVIEW
            )
            SmsGameGraphics.fadeCurtains(this, true)
            return
        }
        val curtainsAreOpened = SmsGameGraphics.curtainsAreOpen(this)
        val duration = if (curtainsAreOpened) {
            SmsGameGraphics.fadeCurtains(this, true)
        } else {
            0
        }

        this.model.delayHandler?.operation("fadecurtains:a", duration.toInt()) {
            when (mediaType) {
                SmsGameModel.MediaType.VIDEO -> {
                    SmsGameGraphics.setBackgroundImageVisibility(this, false)
                    SmsGameGraphics.setBackgroundVideoVisibility(
                        this,
                        viewType == SmsGameModel.ViewType.VIDEOVIEW,
                        SmsGameModel.ViewType.VIDEOVIEW
                    )
                    SmsGameGraphics.setBackgroundVideoVisibility(
                        this,
                        viewType == SmsGameModel.ViewType.PLAYERVIEW,
                        SmsGameModel.ViewType.PLAYERVIEW
                    )
                }

                SmsGameModel.MediaType.IMAGE -> {
                    SmsGameGraphics.setBackgroundImageVisibility(this, true)
                    SmsGameGraphics.setBackgroundVideoVisibility(
                        this,
                        false,
                        SmsGameModel.ViewType.VIDEOVIEW
                    )
                    SmsGameGraphics.setBackgroundVideoVisibility(
                        this,
                        false,
                        SmsGameModel.ViewType.PLAYERVIEW
                    )
                }

                else -> {}
            }
            SmsGameGraphics.fadeCurtains(this, false)
            model.currentMediaTypeDisplayed = mediaType
        }
    }

    companion object {

        fun require(
            activity: Activity,
            story: Story,
            storyType: StoryType,
            phrases: ArrayList<Phrase>,
            links: ArrayList<TableOfLinks.Link>,
            characters: ArrayList<StoryCharacter>,
            messagesSide: ArrayList<Int>,
            tableOfCreatorResources: TableOfCreatorResources
        ): Intent {
            val intent = Intent(activity, SmsGameActivity::class.java)
            intent.putExtra(Data.Companion.Extra.STORY.id, story)
            intent.putExtra(Data.Companion.Extra.STORY_TYPE.id, storyType as Serializable)
            intent.putParcelableArrayListExtra(Data.Companion.Extra.PHRASES.id, phrases)
            intent.putParcelableArrayListExtra(Data.Companion.Extra.LINKS.id, links)
            intent.putParcelableArrayListExtra(Data.Companion.Extra.CHARACTERS.id, characters)
            intent.putIntegerArrayListExtra(Data.Companion.Extra.MESSAGES_SIDE.id, messagesSide)
            intent.putExtra(
                Data.Companion.Extra.TABLE_OF_CREATOR_RESOURCES.id,
                tableOfCreatorResources
            )
            return intent
        }

        fun require(
            intent: Intent,
            resources: TableOfCreatorResources,
            storyAsCard: Game,
            chapters: ArrayList<StoryChapter>,
            storyType: StoryType
        ): Intent {
            intent.putExtra(Data.Companion.Extra.ITEM.id, storyAsCard as Parcelable)
            intent.putExtra(
                Data.Companion.Extra.TABLE_OF_CREATOR_RESOURCES.id,
                resources as Parcelable
            )
            intent.putExtra(Data.Companion.Extra.STORY_TYPE.id, storyType)
            intent.putParcelableArrayListExtra(Data.Companion.Extra.CHAPTERS_ARRAY.id, chapters)
            return intent
        }
    }

    override fun onAdAborted() {

    }

    override fun onAdSuccessfullyWatched() {
        // Display filter + progressbar
        SmsGameGraphics.setLoadingScreenVisibility(this, true)
        this.model.conversation.adapter.reloadLastItem()
        this.model.customer.onSeenAds(this, 30) { isSuccessful, exception ->
            if (this.isFinishing) {
                return@onSeenAds
            }
            // Hide filter + progressbar
            SmsGameGraphics.setLoadingScreenVisibility(this, false)
            // Display success screen
            /*val diamondsBinding =
                LayoutShopBuyValidationBinding.bind(binding.coinsDiamondsValidation.root)

            FingerV2.register(diamondsBinding.buttonContinue, null) {
                diamondsBinding.root.visibility = View.GONE
            }
            SmsGameGraphics.setUnlockItemImageSize(this)
            ShopActivityGraphics.setUnlockDiamondsByWatchingAds(
                this,
                diamondsBinding,
                this.model.requestManager,
                this.model.customer.isUserConnected()
            ) {
                if (this.isFinishing) {
                    return@setUnlockDiamondsByWatchingAds
                }

                ShopActivityGraphics.animateUnlockItem(
                    this,
                    diamondsBinding,
                    this.model.customer.getCoins(),
                    this.model.customer.getDiamonds()
                )
            } */
        }
    }

    override fun onAdRemovedPaid() {

    }

    override fun onErrorFound(code: String?, message: String?, adUnit: String?) {

    }
}
