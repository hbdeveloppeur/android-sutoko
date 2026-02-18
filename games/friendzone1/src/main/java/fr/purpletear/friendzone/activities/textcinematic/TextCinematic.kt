package fr.purpletear.friendzone.activities.textcinematic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.purpletear.friendzone.BuildConfig.VERSION_CODE
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.DiscussionHandler
import fr.purpletear.friendzone.config.Phrase
import purpletear.fr.purpleteartools.*

class TextCinematic : AppCompatActivity() {

    private lateinit var model: TextCinematicModel
    private lateinit var graphics: TextCinematicGraphics
    private var mh = MemoryHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friendzone1_activity_text_cinematic)
        load()
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
        if (model.isFirstStart()) {
            TextCinematicGraphics.setImages(this@TextCinematic, model.requestManager)
            discuss(model.currentPhrase)
        }
    }

    override fun onDestroy() {
        mh.kill()
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
        graphics.mh.kill()
    }

    private fun load() {
        model = TextCinematicModel(
                this
        )
        graphics = TextCinematicGraphics()
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
                    if(!model.collectedTrophies.containsByTrophyId(phrase.trophyId)) {
                        model.collectedTrophies.add(this@TextCinematic, phrase.trophyId, GlobalData.Game.FRIENDZONE.id, VERSION_CODE)
                        model.collectedTrophies.save(this@TextCinematic)
                        val sh  = SimpleSound()
                        sh.prepareAndPlay(this@TextCinematic, com.example.sharedelements.R.raw.deduction, false, 0)
                    }
                    if (model.hasNextPhrase(phrase)) {
                        model.currentPhrase = model.getNextPhrase(phrase)
                        discuss(model.currentPhrase)
                    } else {
                        onTellingFinished()
                    }
                }
            }
            mh.push(runnable)
            mh.run(runnable)

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

                        mh.push(runnable)
                        mh.run(runnable)
                    }
                }
            }

            mh.push(runnable)
            mh.run(runnable)
        }
    }

    private fun onTellingFinished() {
        graphics.fadeText(this@TextCinematic, false, 280)
        TextCinematicGraphics.fadeRateScreen(this@TextCinematic)
    }


    private fun onRateButtonPressed() {
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