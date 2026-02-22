package com.purpletear.smsgame.activities.smsgamevideointroduction

import android.app.Activity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SmsGameTreeStructure
import com.example.sharedelements.Data
import com.purpletear.smsgame.activities.smsgamevideointroduction.helpers.SmsGameVideoIntroTimeline
import purpletear.fr.purpleteartools.TableOfSoundsPlayer
import purpletear.fr.purpleteartools.DelayHandler

class SmsGameVideoIntroModel(activity: Activity) {

    // Conversation object
    private var timeline: SmsGameVideoIntroTimeline
    private var soundPlayer: TableOfSoundsPlayer
    var requestManager: RequestManager
        private set
    var storyId: Int
    var delayHandler: DelayHandler
    var currentSoundName: String = "none"
        private set

    init {
        storyId = activity.intent?.getIntExtra(Data.Companion.Extra.STORY_ID.id, -1) ?: -1
        timeline = SmsGameVideoIntroTimeline(activity)
        delayHandler = DelayHandler()
        soundPlayer = TableOfSoundsPlayer()
        requestManager = Glide.with(activity)
    }

    fun startSound(activity: Activity, soundName: String, delay: Int) {
        this.delayHandler.operation("onFoundSound : $soundName", delay) {
            val path = SmsGameTreeStructure.getMediaFilePath(activity, this.storyId.toString(), soundName)
            soundPlayer.addToPreloadList(path)
            soundPlayer.preload(activity)
            soundPlayer.play(path, onStart@{
                this.currentSoundName = path
                if (it) {
                    this.soundPlayer.fadeInSound(
                        activity,
                        this.currentSoundName,
                        3000f,
                        delayHandler
                    )
                }
            }, onFinish@{
                this.currentSoundName = "none"
            }, 0f)
        }
    }

    fun fadeSoundOut(activity: Activity, duration: Int) {
        this.soundPlayer.fadeOutSound(
            activity,
            currentSoundName,
            duration.toFloat(),
            this.delayHandler
        )
    }

    fun fadeSoundIn(activity: Activity, duration: Int) {
        this.soundPlayer.fadeInSound(
            activity,
            currentSoundName,
            duration.toFloat(),
            this.delayHandler
        )
    }


    fun start(activity: Activity) {
        this.timeline.start(activity)
    }

    fun onPause() {
        this.timeline.stop()
        this.soundPlayer.onStop()
    }

    fun onResume(activity: Activity) {
        this.start(activity)
    }

    fun onDestroy() {
        this.delayHandler.stop()
        this.soundPlayer.onStop()
        this.timeline.stop()
    }

}