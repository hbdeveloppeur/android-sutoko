package com.purpletear.smsgame.activities.smsgame

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.Data
import com.example.sharedelements.SutokoParams
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Conversation
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.objects.StoryHelper
import com.purpletear.smsgame.activities.smsgame.objects.StoryMetadata
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCharacters
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.GameMetadata
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.PurpleExoPlayer
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSoundsPlayer
import purpletear.fr.purpleteartools.TableOfSymbols
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Keep
class SmsGameModel(activity: SmsGameActivity) {
    var requestManager: RequestManager = Glide.with(activity)
        private set
    var card: Game
    var waitForUserInput = false
    var waitingInputCode: String? = null
    var customer: Customer = Customer(callbacks = activity)

    var storyType: StoryType
        private set
    var storyMetadata: StoryMetadata
    var conversation: Conversation
    var symbols: TableOfSymbols
    var story: Story? = null
    var tableOfPlayer: TableOfSoundsPlayer
    var videoPlayer: PurpleExoPlayer? = null
    var currentMediaTypeDisplayed: MediaType = MediaType.NONE
    var delayHandler: DelayHandler? = null
    private var ratingTimes: Int = 0

    companion object {
        var storyId: Int = -1
        var chapterCode: String = "-1"
        var version: String = "-1"
        var storyType: String = "none"
    }

    enum class MediaType {
        NONE,
        IMAGE,
        VIDEO,
    }

    enum class ViewType {
        NONE,
        VIDEOVIEW,
        PLAYERVIEW,
        IMAGEVIEW
    }

    init {
        storyType =
            (activity.intent.getSerializableExtra(Data.Companion.Extra.STORY_TYPE.id) as StoryType?)
                ?: StoryType.OFFICIAL_STORY
        delayHandler = DelayHandler()

        when (storyType) {
            StoryType.OTHER_USER_STORY -> {
                story = activity.intent.getParcelableExtra(Data.Companion.Extra.STORY.id)
                    ?: throw IllegalStateException()
                FirebaseCrashlytics.getInstance().setCustomKey("story_id", (story?.id ?: "-1") as String)
                card = Game(metadata = GameMetadata(title = story!!.title))
                storyMetadata = StoryMetadata(
                    card.id,
                    card.metadata.title,
                    card.metadata.description ?: "",
                    "",
                    11f,
                    ""
                )
            }

            else -> {
                card = activity.intent.getParcelableExtra(Data.Companion.Extra.ITEM.id)
                    ?: throw IllegalStateException("Card not found.")
                FirebaseCrashlytics.getInstance().setCustomKey("story_id", card.id)

                storyMetadata = StoryMetadata(
                    card.id,
                    card.metadata.title,
                    card.metadata.description ?: "",
                    "",
                    11f,
                    ""
                )
                if (storyType == StoryType.OFFICIAL_STORY) {
                    this.customer.read(activity)
                }
            }
        }

        // SmsGameNotificationHelper.cancelNotification(activity, card.id)
        symbols = TableOfSymbols(card.id.hashCode())
        symbols.read(activity)
        symbols.removeFromASpecificChapterNumber(card.id.hashCode(), symbols.chapterNumber)
        symbols.addOrSet(card.id.hashCode(), "os", "android")

        FirebaseCrashlytics.getInstance()
            .setCustomKey("langCode", Language.determineLangDirectory())
        FirebaseCrashlytics.getInstance().setCustomKey("storyType", storyType.name)
        FirebaseCrashlytics.getInstance()
            .setCustomKey("storyVersion", symbols.getStoryVersion(this.card.id.hashCode()))

        SmsGameModel.storyId = card.id.hashCode()
        SmsGameModel.version = symbols.getStoryVersion(this.card.id.hashCode())
        SmsGameModel.storyType = storyType.name
        SmsGameModel.chapterCode = symbols.chapterCode

        FirebaseCrashlytics.getInstance().setCustomKey("chapterCode", symbols.chapterCode)

        val sutokoParams = SutokoParams()
        sutokoParams.read(activity)
        val characters = TableOfCharacters(
            activity, storyMetadata.storyId,
            symbols.chapterCode, Language.determineLangDirectory(), storyType
        )

        try {
            conversation = if (storyType == StoryType.OTHER_USER_STORY) {
                Conversation(
                    activity, requestManager,
                    ArrayList(),
                    sutokoParams, card, storyMetadata,
                    characters, storyType,
                    activity.intent.getParcelableArrayListExtra<Phrase>(Data.Companion.Extra.PHRASES.id)!!,
                    activity.intent.getParcelableArrayListExtra<TableOfLinks.Link>(Data.Companion.Extra.LINKS.id)!!,
                    activity.intent.getParcelableArrayListExtra<StoryCharacter>(Data.Companion.Extra.CHARACTERS.id)!!,
                    activity.intent.getIntegerArrayListExtra(Data.Companion.Extra.MESSAGES_SIDE.id)!!,
                    symbols
                )
            } else {
                Conversation(
                    activity,
                    requestManager,
                    card,
                    storyMetadata,
                    sutokoParams,
                    characters,
                    activity.intent.getParcelableArrayListExtra(Data.Companion.Extra.CHAPTERS_ARRAY.id)!!,
                    storyType,
                    symbols
                )
            }
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException()
        }

        tableOfPlayer = TableOfSoundsPlayer()
        conversation.start(storyType) onError@{
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    fun addSymbols(name: String, value: String) {
        this.symbols.addOrSet(this.card.id.hashCode(), name, value)
    }


    fun requestUserInput(activity: SmsGameActivity, onSuccess: (input: String) -> Unit) {
        if (activity.isFinishing) {
            return
        }

        val builder = android.app.AlertDialog.Builder(activity)
        @SuppressLint("InflateParams") val linearLayout = activity.layoutInflater.inflate(
            R.layout.sutoko_edittext_firstname_,
            null
        ) as LinearLayout
        val editText = linearLayout.findViewById<EditText>(R.id.sutoko_edittext_friendzoned)
        linearLayout.findViewById<TextView>(R.id.sutoko_edittext_subtitle).text =
            activity.getString(R.string.sutoko_chose_an_answer)
        editText.hint = activity.getString(R.string.sutoko_answer)
        builder.setTitle(activity.getString(R.string.sutoko_answer))
        builder.setView(linearLayout)
        builder.setNegativeButton(activity.getString(R.string.cancel)) { _, _ -> }
        builder.setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
            Handler(Looper.getMainLooper()).post {
                var txt = editText.text.toString()
                if (txt.isBlank()) {
                    txt = ""
                }
                onSuccess(txt)
            }
        }

        val dialog = builder.create()
        dialog.show()
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.cancel()
                onSuccess(editText.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun notifyRead(activity: SmsGameActivity) {
        if (this.storyType != StoryType.OTHER_USER_STORY) {
            return
        }
        StoryHelper.read(activity)
        StoryHelper.addRead(this.story ?: return) {
            StoryHelper.save(activity)
        }
    }

    fun getMyCurrentStoryMark(): Int {
        return try {
            Integer.parseInt(this.symbols.get(this.card.id.hashCode(), "rate") ?: "0")
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Saves the game
     * @param activity SmsGameActivity
     */
    fun save(activity: SmsGameActivity) {
        this.symbols.save(activity)
    }

    fun insertChapterSwitchCell(activity: SmsGameActivity) {
        val p = Phrase(Phrase.Type.nextChapter)
        p.code = this.conversation.currentPhrase?.code ?: this.symbols.chapterCode
        this.conversation.adapter.insert(p)
    }

    fun goToNextChapter(activity: SmsGameActivity) {
        delayHandler?.stop()
        val intent = Intent()
        intent.putExtra(Data.Companion.Extra.TABLE_OF_SYMBOLS.id, this.symbols as Parcelable)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    fun rateStory(activity: Activity, mark: Int) {
        val currentMark = getMyCurrentStoryMark()
        if (mark == currentMark) {
            return
        }
        this.conversation.adapter.setMark(mark)
        this.symbols.addOrSet(card.id.hashCode(), "rate", mark.toString())
        this.symbols.save(activity)

        if (this.ratingTimes > 3) {
            return
        }

        // rate(mark, card.id)
        ratingTimes++
    }


//    fun rate(mark: Int, storyId: Int) {
//        Std.debug("https://secure.sutoko.app/story/rate?si=${storyId}&lc=${Language.determineLangDirectory()}&m=${mark}&o=0")
//        "https://secure.sutoko.app/story/rate?si=${storyId}&lc=${Language.determineLangDirectory()}&m=${mark}&o=0"
//            .httpGet()
//            .timeout(4000)
//            .response { request, response, result ->
//                Std.debug(response)
//                Std.debug(result)
//            }
//    }

    fun isMainCharacter(phrase: Phrase): Boolean {
        return phrase.id_author != -1 && conversation.characters.getCharacter(phrase.id_author).isMainCharacter
    }

    fun determinePhraseTypingDuration(phrase: Phrase): Int {
        if (isMainCharacter(phrase)) {
            return 0
        }
        val duration = phrase.sentence.length * 1800 / 30
        return limitValue(duration, 1000, 3260)
    }

    private fun limitValue(value: Int, min: Int, max: Int): Int {
        if (value < min) {
            return min
        }

        if (value > max) {
            return max
        }

        return value
    }


    fun playTypingSound(activity: SmsGameActivity) {
        val file: File
        try {
            file = File(Std.getFileFromAssets(activity, "typing.mp3").path)
        } catch (e: IOException) {
            return
        }
        val path = file.path
        tableOfPlayer.addToPreloadList(path)
        tableOfPlayer.preload(activity)
        tableOfPlayer.play(path, onStart@{

        }, onFinish@{
            tableOfPlayer.remove(path)
        }, true)
    }

    fun shouldDisplayStoryImage(): Boolean {
        return when (storyType) {
            StoryType.OFFICIAL_STORY -> true
            else -> false
        }
    }

    fun operation(activity: SmsGameActivity, name: String, delay: Int, runnable: () -> Unit) {
        delayHandler?.operation(name, delay, runnable)
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }

}