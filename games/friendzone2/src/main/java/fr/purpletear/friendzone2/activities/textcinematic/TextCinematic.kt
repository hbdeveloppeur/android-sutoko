package fr.purpletear.friendzone2.activities.textcinematic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.BuildConfig.VERSION_CODE
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.DiscussionHandler
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.configs.Var
import purpletear.fr.purpleteartools.*

class TextCinematic : AppCompatActivity() {

    private lateinit var model : TextCinematicModel
    private lateinit var graphics : TextCinematicGraphics
    private var mh = MemoryHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textcinematic)
        load()
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
        if(model.isFirstStart()) {
            TextCinematicGraphics.setImages(this@TextCinematic, model.requestManager)
            discuss(model.currentPhrase)
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onDestroy() {
        mh.kill()
        model.pauseSound(this)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        graphics.mh.kill()
    }

    private fun load() {
        model = TextCinematicModel(
                this,
                intent.getParcelableExtra("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)
        )

        graphics = TextCinematicGraphics()
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
            val values = phrase.answerCondition
            val condition = values[0]!!.replace("[", "").replace("]", "").replace(" ", "").split("==".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val mThen = Integer.parseInt(values[1]!!)
            val mElse = Integer.parseInt(values[2]!!)

            val v = Var(condition[0], condition[1], model.symbols.chapterNumber)
            val next: Phrase = if (model.symbols.condition(GlobalData.Game.FRIENDZONE2.id, v.name, v.value)) {
                model.phrases.getPhrase(mThen)
            } else {
                model.phrases.getPhrase(mElse)
            }
            model.currentPhrase = next

            discuss(next)
            return
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", phrase.isTrophy)) {

            val runnable = object : Runnable2("Le joueur débloque un trophée", 1280) {
                override fun run() {
                    if(!model.collectedTrophies.containsByTrophyId(phrase.trophyId)) {
                        model.collectedTrophies.add(this@TextCinematic, phrase.trophyId, GlobalData.Game.FRIENDZONE2.id, VERSION_CODE)
                        model.collectedTrophies.save(this@TextCinematic)
                        val sh  = SimpleSound()
                        sh.prepareAndPlay(this@TextCinematic, com.example.sharedelements.R.raw.deduction, false, 0)
                    }
                    if(model.hasNextPhrase(phrase)) {
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
                                if(model.hasNextPhrase(phrase)) {
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

        if(!model.isEnd()) {
            graphics.fadeOutImage(this@TextCinematic) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        } else {
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