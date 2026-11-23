package friendzone3.purpletear.fr.friendzon3.textcinematic

import com.purpletear.sutoko.analytics.data.statistics.LegacyChapterReports

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import friendzone3.purpletear.fr.friendzon3.*
import friendzone3.purpletear.fr.friendzon3.BuildConfig
import friendzone3.purpletear.fr.friendzon3.R
import friendzone3.purpletear.fr.friendzon3.custom.Phrase
import friendzone3.purpletear.fr.friendzon3.handlers.DiscussionHandler
import friendzone3.purpletear.fr.friendzon3.handlers.NavigationHandler
import purpletear.fr.purpleteartools.*

class TextCinematic : AppCompatActivity() {

    private lateinit var model: TextCinematicModel
    private lateinit var graphics: TextCinematicGraphics
    private val sh = friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler()
    private val scheduler = CinematicScheduler()
    private lateinit var progress: CinematicProgressView
    private var endResultCode : Int  = 999
    private var prevent : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fz3_activity_text_cinematic)
        load()
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) {
            LegacyChapterReports.entered(this,159,friendzone3.purpletear.fr.friendzon3.config.Language.determineLangDirectory(),model.symbols.chapterCode)
            LegacyChapterReports.observe(this,159,friendzone3.purpletear.fr.friendzon3.config.Language.determineLangDirectory(),model.symbols.chapterCode)
        }
        progress = findViewById(R.id.cinematic_progress)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_CANCELED) {
            setResult(Activity.RESULT_CANCELED)
            prevent = true
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            TextCinematicGraphics.fadeFilter(this, true)
        } else {
            TextCinematicGraphics.fadeFilter(this, false)
        }
    }

    override fun onResume() {
        super.onResume()
        scheduler.resume()
        progress.resume()
        if (!prevent && model.isFirstStart()) {
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
        sh.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()

        val name = cinematicSoundName()
        sh.generate(name, this, false).play(name)
    }

    override fun onPause() {
        super.onPause()
        scheduler.pause()
        progress.pause()
        sh.pause(cinematicSoundName())
    }

    private fun cinematicSoundName(): String =
        if (model.symbols.chapterCode == "1a") "bg_cinematic_1a" else "bg_cinematic"

    private fun load() {
        model = TextCinematicModel(
                this
        )
        graphics = TextCinematicGraphics(scheduler)
    }

    /**
     * Brows the discussion
     */
    private fun discuss(phrase: Phrase) {

        if (DiscussionHandler.execute("Ecran de fin", phrase.isAnnouncement)) {
            progress.complete()
            setResult(Activity.RESULT_CANCELED)
            val i = Intent(this, End::class.java)
            i.flags = i.flags or Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivityForResult(i, endResultCode)
            return
        }

        if (DiscussionHandler.execute("Affichage d'une image de fond", phrase.isBackgroundImage)) {
            graphics.setImage(this@TextCinematic, model.requestManager, model.getImageId(this@TextCinematic, "friendzone3_" + phrase.backgroundImageName)) {
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
        if (!com.example.sharedelements.SutokoSharedElementsData.SHOULD_FORCE_CHAPTER && model.symbols.chapterCode.lowercase(java.util.Locale.ROOT) in setOf("9a", "9b", "9c", "9d")) { LegacyChapterReports.completed(this,159,friendzone3.purpletear.fr.friendzon3.config.Language.determineLangDirectory(),model.symbols.chapterCode); LegacyChapterReports.storyFinished(this,159,friendzone3.purpletear.fr.friendzon3.config.Language.determineLangDirectory(),model.symbols.chapterCode) }
        progress.complete()
        graphics.fadeText(this@TextCinematic, false, 280)
        TextCinematicGraphics.fadeFilter(this, false)
        setResult(Activity.RESULT_OK)
        finish()
    }
}