package fr.purpletear.friendzone2.activities.textcinematic

import com.purpletear.sutoko.analytics.data.statistics.LegacyChapterReports

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.DiscussionHandler
import fr.purpletear.friendzone2.configs.Phrase
import purpletear.fr.purpleteartools.*

class TextCinematic : AppCompatActivity() {

    private lateinit var model : TextCinematicModel
    private lateinit var graphics : TextCinematicGraphics
    private val scheduler = CinematicScheduler()
    private lateinit var progress: CinematicProgressView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textcinematic)
        load()
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) {
            LegacyChapterReports.entered(this,161,fr.purpletear.friendzone2.tables.Language.determineLangDirectory(),model.symbols.chapterCode)
            LegacyChapterReports.observe(this,161,fr.purpletear.friendzone2.tables.Language.determineLangDirectory(),model.symbols.chapterCode)
        }
        progress = findViewById(R.id.cinematic_progress)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(hasFocus) {
            TextCinematicGraphics.fadeFilter(this)
            model.startSound(this)
        } else {
            model.pauseSound(this)
        }
    }

    override fun onResume() {
        super.onResume()
        scheduler.resume()
        progress.resume()
        if(model.isFirstStart()) {
            progress.start(model.cinematicDurationMillis())
            TextCinematicGraphics.setImages(this@TextCinematic, model.requestManager)
            discuss(model.currentPhrase)
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onDestroy() {
        scheduler.clear()
        progress.stop()
        model.pauseSound(this)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        scheduler.pause()
        progress.pause()
    }

    private fun load() {
        model = TextCinematicModel(
                this,
                intent.getParcelableExtra("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)
        )

        graphics = TextCinematicGraphics(scheduler)
        TextCinematicGraphics.setStatsText(this@TextCinematic)
        Finger.registerListener(this, R.id.stats_button_continue, ::onContinueButtonPressed)
        Finger.registerListener(this, R.id.end_rate, ::onRateButtonPressed)
    }

    /**
     * Brows the discussion
     */
    private fun discuss(phrase : Phrase) {

        if(DiscussionHandler.execute("Affichage d'une image de fond", phrase.isBackgroundImage)) {
            graphics.setImage(this@TextCinematic, model.requestManager, model.getImageId(this@TextCinematic, phrase.backgroundImageName)) {
                if(model.hasNextPhrase(phrase)) {
                    model.currentPhrase = model.getNextPhrase(phrase)
                    discuss(model.currentPhrase)
                } else {
                    onTellingFinished()
                }
            }
            return
        }

        if(DiscussionHandler.execute("Condition", phrase.`is`(Phrase.Type.condition))) {
            val next = model.conditionDestination(phrase)
            model.currentPhrase = next

            discuss(next)
            return
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", phrase.isTrophy)) {

            val runnable = object : Runnable2("Le joueur débloque un trophée", 1280) {
                override fun run() {
                    if(model.hasNextPhrase(phrase)) {
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
                                if(model.hasNextPhrase(phrase)) {
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
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER && model.isEnd()) { LegacyChapterReports.completed(this,161,fr.purpletear.friendzone2.tables.Language.determineLangDirectory(),model.symbols.chapterCode); LegacyChapterReports.storyFinished(this,161,fr.purpletear.friendzone2.tables.Language.determineLangDirectory(),model.symbols.chapterCode) }
        graphics.fadeText(this@TextCinematic, false, 280)

        if(!model.isEnd()) {
            graphics.fadeOutImage(this@TextCinematic) {
                progress.complete()
                setResult(Activity.RESULT_OK)
                finish()
            }
        } else {
            progress.complete()
            TextCinematicGraphics.fadeStatsScreen(this@TextCinematic, true)
            TextCinematicGraphics.animateStatsBar(this@TextCinematic, 1)
            TextCinematicGraphics.animateStatsBar(this@TextCinematic, 2)
            TextCinematicGraphics.animateStatsBar(this@TextCinematic, 3)
            TextCinematicGraphics.animateStatsBar(this@TextCinematic, 4)
        }
    }

    private fun onContinueButtonPressed() {
        Finger.disableListener(this, R.id.stats_button_continue)
        TextCinematicGraphics.fadeStatsScreen(this@TextCinematic, false)
        TextCinematicGraphics.fadeRateScreen(this@TextCinematic)
    }

    private fun onRateButtonPressed() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}