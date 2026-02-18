package com.purpletear.smsgame.activities.smsgamevideointroduction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sharedelements.SmsGameTreeStructure
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.purpletear.smsgame.activities.smsgamevideointroduction.helpers.SmsGameVideoIntroCallbacks
import com.purpletear.smsgame.databinding.ActivitySmsGameVideoIntroBinding


class SmsGameVideoIntro : AppCompatActivity(), SmsGameVideoIntroCallbacks {
    lateinit var binding: ActivitySmsGameVideoIntroBinding
    private var model: SmsGameVideoIntroModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // SimpleStoryPreviewGraphics.setStatusBar(window)
        try {
            model = SmsGameVideoIntroModel(this)
        } catch (e: Exception) {
            Firebase.crashlytics.log("Error : ${e.message}")
            setResult(RESULT_OK)
            finish()
            return
        }
        binding = ActivitySmsGameVideoIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onDestroy() {
        this.model?.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        this.model?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        this.model?.onResume(this)
    }

    override fun onBackPressed() {

        // Finish result CANCELED
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onImageFound(filename: String) {
        val duration = this.hideVisualsIfNecessary()
        val path = SmsGameTreeStructure.getMediaFilePath(this, model?.storyId ?: return, filename)

        this.model?.delayHandler?.operation("onImageFound", duration) {
            SmsGameVideoIntroGraphics.setImage(
                this,
                this.model?.requestManager ?: return@operation,
                path
            )
            SmsGameVideoIntroGraphics.setVisibilityBackgroundImage(this, true, duration)
        }
    }

    override fun onVideoFound(filename: String, isLooping: Boolean) {
        // fade video out if displayed
        val duration: Int = this.hideVisualsIfNecessary()
        this.model?.delayHandler?.operation("onVideoFound", duration) {

            SmsGameVideoIntroGraphics.setVideo(
                this,
                SmsGameTreeStructure.getMediaFilePath(
                    this,
                    model?.storyId ?: return@operation,
                    filename
                ),
                isLooping
            ) {
                SmsGameVideoIntroGraphics.setVisibilityFilterVideo(this, true, duration)
            }
            // fade video in
        }
    }

    override fun onSoundFound(filename: String, delay: Int) {
        // Start sounds.
        this.model?.startSound(this, filename, delay)

    }

    override fun onTextFound(text: String, duration: Int) {
        // Fade text out if displayed
        var d: Int = 0
        if (SmsGameVideoIntroGraphics.isTextVisible(this.binding)) {
            d = 1280
            SmsGameVideoIntroGraphics.setVisibilityText(this, false, d)
        }
        this.model?.delayHandler?.operation("onTextFound", d) {
            // then change text
            SmsGameVideoIntroGraphics.setText(this.binding, text)
            // then fade text in
            SmsGameVideoIntroGraphics.setVisibilityText(this, true, duration)
        }
    }

    override fun onCompletion() {
        // Fade filter in slowly
        val duration: Int = 3280

        SmsGameVideoIntroGraphics.fadeOut(this)
        // Fade sound out slowly
        this.model?.fadeSoundOut(this, duration + 1000)
        // SmsGameVideoIntroGraphics.setVisibilityFilterVideo(this, false, duration)
        // Finish result okay
        this.model?.delayHandler?.operation("onCompletion", duration + 1000) {
            this@SmsGameVideoIntro.setResult(RESULT_OK)
            this@SmsGameVideoIntro.finish()
        }
    }

    private fun hideVisualsIfNecessary(): Int {
        var duration = 0
        if (SmsGameVideoIntroGraphics.isBackgroundImageVisible(this.binding)) {
            duration = 1280
            SmsGameVideoIntroGraphics.setVisibilityBackgroundImage(this, false, duration)
        }

        if (SmsGameVideoIntroGraphics.isVideoVisible(this.binding)) {
            duration = 1280
            // Fade out
            SmsGameVideoIntroGraphics.setVisibilityFilterVideo(this, false, duration)
        }
        return duration
    }

}