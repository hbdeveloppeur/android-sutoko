package friendzone3.purpletear.fr.friendzon3.textcinematic

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
    private var sh = SoundHandler(Data.assetsDirectoryName)
    private var mh = MemoryHandler()
    private var endResultCode : Int  = 999
    private var prevent : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fz3_activity_text_cinematic)
        load()
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
        if (!prevent && model.isFirstStart()) {
            TextCinematicGraphics.setImages(this@TextCinematic, model.requestManager)
            discuss(model.currentPhrase)
        }
    }

    override fun onDestroy() {
        mh.kill()
        model.sound.stop()
        model.sound.onDestroy()
        graphics.mh.kill()
        super.onDestroy()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()

        if(model.symbols.chapterCode == "1a") {
            sh.generateFromExternalStorage(Data.selectSound(this, "fz3_bg_cinematic_"+model.symbols.chapterCode), this, false)
            sh.play(Data.selectSound(this, "fz3_bg_cinematic_"+model.symbols.chapterCode))
        } else {
            sh.generateFromExternalStorage(Data.selectSound(this, "fz3_bg_cinematic"), this, false)
            sh.play(Data.selectSound(this, "fz3_bg_cinematic"))
        }
    }

    override fun onPause() {
        super.onPause()

        if(model.symbols.chapterCode == "1a") {
            sh.pause(Data.selectSound(this, "fz3_bg_cinematic_"+model.symbols.chapterCode))
        } else {
            sh.pause(Data.selectSound(this, "fz3_bg_cinematic"))
        }
    }

    private fun load() {
        model = TextCinematicModel(
                this
        )
        graphics = TextCinematicGraphics()
    }

    /**
     * Brows the discussion
     */
    private fun discuss(phrase: Phrase) {

        if (DiscussionHandler.execute("Ecran de fin", phrase.isAnnouncement)) {
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
                    if(!model.collectedTrophies.containsByTrophyId(phrase.trophyId)) {
                        model.collectedTrophies.add(this@TextCinematic, phrase.trophyId, GlobalData.Game.FRIENDZONE3.id, BuildConfig.VERSION_CODE)
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
        TextCinematicGraphics.fadeFilter(this, false)
        setResult(Activity.RESULT_OK)
        finish()
    }
}