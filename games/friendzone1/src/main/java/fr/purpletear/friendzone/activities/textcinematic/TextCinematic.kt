package fr.purpletear.friendzone.activities.textcinematic

import com.purpletear.sutoko.analytics.data.statistics.LegacyChapterReports

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory
import fr.purpletear.friendzone.BuildConfig.VERSION_CODE
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.DiscussionHandler
import fr.purpletear.friendzone.config.Phrase
import purpletear.fr.purpleteartools.*

class TextCinematic : AppCompatActivity() {

    private lateinit var model: TextCinematicModel
    private lateinit var graphics: TextCinematicGraphics
    private val scheduler = CinematicScheduler()
    private lateinit var progress: CinematicProgressView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friendzone1_activity_text_cinematic)
        load()
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) {
            LegacyChapterReports.entered(this,162,fr.purpletear.friendzone.tables.Language.determineLangDirectory(),model.symbols.chapterCode)
            LegacyChapterReports.observe(this,162,fr.purpletear.friendzone.tables.Language.determineLangDirectory(),model.symbols.chapterCode)
        }
        progress = findViewById(R.id.cinematic_progress)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            TextCinematicGraphics.fadeFilter(this)
            model.startSound(this)
        } else {
            model.pauseSound()
        }
    }

    override fun onResume() {
        super.onResume()
        scheduler.resume()
        progress.resume()
        if (model.isFirstStart()) {
            progress.start(model.cinematicDurationMillis())
            TextCinematicGraphics.setImages(this@TextCinematic, model.requestManager)
            discuss(model.currentPhrase)
        }
    }

    override fun onDestroy() {
        scheduler.clear()
        progress.stop()
        model.sound.stop()
        model.sound.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        scheduler.pause()
        progress.pause()
    }

    private fun load() {
        model = TextCinematicModel(
                this
        )
        graphics = TextCinematicGraphics(scheduler)
        Finger.registerListener(this, R.id.end_rate, ::onRateButtonPressed)
        paramsRequest()
    }

    /**
     * Brows the discussion
     */
    private fun discuss(phrase: Phrase) {

        if (DiscussionHandler.execute("Affichage d'une image de fond", phrase.isBackgroundImage)) {
            graphics.setImage(this@TextCinematic, model.requestManager, model.getImageId(this@TextCinematic, phrase.backgroundImageName)) {
                if (model.hasNextPhrase(phrase)) {
                    model.currentPhrase = model.getNextPhrase(phrase)
                    discuss(model.currentPhrase)
                } else {
                    onTellingFinished()
                }
            }
            return
        }


        if (DiscussionHandler.execute("Le joueur débloque un trophée", phrase.isTrophy)) {

            val runnable = object : Runnable2("Le joueur débloque un trophée", 1280) {
                override fun run() {
                    if (model.hasNextPhrase(phrase)) {
                        model.currentPhrase = model.getNextPhrase(phrase)
                        discuss(model.currentPhrase)
                    } else {
                        onTellingFinished()
                    }
                }
            }
            scheduler.schedule(runnable.duration, runnable)

            return
        }

        graphics.fadeText(this@TextCinematic, false) {
            val runnable = object : Runnable2("Attente avant apparition d'un texte ${phrase.id}", phrase.seen) {
                override fun run() {
                    graphics.setText(this@TextCinematic, phrase.sentence) {
                        val runnable = object : Runnable2("Attente avant disparition d'un texte ${phrase.id}", phrase.wait) {
                            override fun run() {
                                if (model.hasNextPhrase(phrase)) {
                                    model.currentPhrase = model.getNextPhrase(phrase)
                                    discuss(model.currentPhrase)
                                } else {
                                    onTellingFinished()
                                }
                            }
                        }

                        scheduler.schedule(runnable.duration, runnable)
                    }
                }
            }

            scheduler.schedule(runnable.duration, runnable)
        }
    }

    private fun onTellingFinished() {
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) { LegacyChapterReports.completed(this,162,fr.purpletear.friendzone.tables.Language.determineLangDirectory(),model.symbols.chapterCode); LegacyChapterReports.storyFinished(this,162,fr.purpletear.friendzone.tables.Language.determineLangDirectory(),model.symbols.chapterCode) }
        progress.complete()
        graphics.fadeText(this@TextCinematic, false, 280)
        TextCinematicGraphics.fadeRateScreen(this@TextCinematic)
    }


    private fun onRateButtonPressed() {
        val reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewManager.launchReviewFlow(this, request.result)
            } else {
                openStorePage()
            }
        }
    }

    private fun openStorePage() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    /**
     * Request the EndParams
     * @see fr.purpletear.friendzone.Data
     */
    private fun paramsRequest() {
        TextCinematicGraphics.socialNetworkVisibility(this@TextCinematic, false)
    }
}