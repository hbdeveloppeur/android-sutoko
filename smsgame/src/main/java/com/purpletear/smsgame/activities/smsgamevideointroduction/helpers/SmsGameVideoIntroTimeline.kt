package com.purpletear.smsgame.activities.smsgamevideointroduction.helpers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.example.sharedelements.BuildConfig
import com.example.sutokosharedelements.Data
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.smsgame.activities.smsgame.tables.TableOfPhrases
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.Std
import java.lang.Exception
import java.lang.IllegalStateException

class SmsGameVideoIntroTimeline(activity: Activity) {

    val callback: SmsGameVideoIntroCallbacks = activity as SmsGameVideoIntroCallbacks
    private val delayHandler = DelayHandler()
    private var currentPhrase: Phrase? = null
    private var startingId: Int = 0

    private val phrases: TableOfPhrases = TableOfPhrases(activity, null, "", "")
    private val links: TableOfLinks = TableOfLinks(activity, null, "", "")

    private var turn = false

    init {
        phrases.array =
            activity.intent?.getParcelableArrayListExtra(Data.Companion.Extra.PHRASES.id)
                ?: throw IllegalStateException()
        links.links = activity.intent?.getParcelableArrayListExtra(Data.Companion.Extra.LINKS.id)
            ?: throw IllegalStateException()
        startingId = if (phrases.array.size > 0) phrases.array[0].id else 0
    }

    fun stop() {
        this.delayHandler.stop()
    }

    @Throws
    fun start(activity: Activity) {
        try {
            this.getNext()
        } catch (e: Exception) {
            Std.debug(e)
            this.currentPhrase = null
        }

        this.turn = true
        if (null == currentPhrase) {

            Handler(Looper.getMainLooper()).post { this.callback.onCompletion() }
            return
        }


        condition("Found sound", this.currentPhrase!!.isSound) {
            this.callback.onSoundFound(this.currentPhrase!!.soundName, this.currentPhrase!!.seen)
            val totalDuration = this.currentPhrase!!.seen + this.currentPhrase!!.wait
            this.delayHandler.operation(it, totalDuration) {
                start(activity)
            }
            return@condition
        }

        condition("Show background video", this.currentPhrase!!.isVideo) {
            this.callback.onVideoFound(currentPhrase!!.getVideo, true)
            val totalDuration = this.currentPhrase!!.seen + this.currentPhrase!!.wait
            this.delayHandler.operation(it, totalDuration + 1200) {
                start(activity)
            }
            return@condition
        }

        condition("Show background image", this.currentPhrase!!.isBackgroundImage) {
            this.callback.onImageFound(currentPhrase!!.backgroundImageName)
            val totalDuration = this.currentPhrase!!.seen + this.currentPhrase!!.wait
            this.delayHandler.operation(it, totalDuration + 1200) {
                start(activity)
            }
            return@condition
        }

        condition("Displays text", this.currentPhrase!!.isNarratorSentence) {
            this.callback.onTextFound(currentPhrase!!.getNarratorSentence, currentPhrase!!.seen)
            val totalDuration = this.currentPhrase!!.seen + this.currentPhrase!!.wait
            this.delayHandler.operation(it, totalDuration) {
                start(activity)
            }
            return@condition
        }

        if (turn) {
            this.callback.onCompletion()
        }
    }

    private fun condition(
        log: String,
        condition: Boolean,
        operation: (processName: String) -> Unit
    ) {
        if (!turn) {
            return
        }
        if (condition) {
            turn = false
            if (BuildConfig.DEBUG) {
                Std.debug(log)
            }
            Handler(Looper.getMainLooper()).post {
                operation(log)
            }
        }
    }

    private fun condition(log: String, operation: (processName: String) -> Unit) {
        if (!turn) {
            return
        }
        if (BuildConfig.DEBUG) {
            Std.debug(log)
        }
        Handler(Looper.getMainLooper()).post {
            operation(log)
        }
    }

    @Throws
    private fun getNext() {
        if (this.currentPhrase == null) {
            this.currentPhrase = this.phrases.getPhrase(startingId)
            return
        }
        val link = links.getDest(this.currentPhrase?.id ?: startingId)
        if (link.size == 0) {
            throw IllegalStateException()
        }
        this.currentPhrase = phrases.getPhrase(link[0])
    }
}