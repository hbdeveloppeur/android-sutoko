/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.friendzone.BuildConfig.VERSION_CODE
import fr.purpletear.friendzone.Data
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.activities.game.Game
import fr.purpletear.friendzone.config.DiscussionHandler
import fr.purpletear.friendzone.config.Phrase
import fr.purpletear.friendzone.config.Var
import fr.purpletear.friendzone.tables.Character
import fr.purpletear.friendzone.tables.TableOfCharacters
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Runnable2
import purpletear.fr.purpleteartools.SimpleSound
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.Video
import kotlin.random.Random
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Main : AppCompatActivity(), MainInterface {

    /**
     * Handles the model settings
     * @see MainModel
     */
    private lateinit var model: MainModel

    /**
     * Handles the graphic settings
     * @see MainGraphics
     */
    private lateinit var graphics: MainGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        Std.debug("[STATE] Main : onCreate")
        super.onCreate(savedInstanceState)
        val symbols: TableOfSymbols = intent.getParcelableExtra("symbols")!!
        setContentView(MainModel.getGameLayout(symbols.chapterCode))

        // Enable edge-to-edge and apply system bars (status + navigation) as padding
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val contentView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        load(symbols)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("symbols", model.symbols)
        outState.putParcelable(
            "recyclerview",
            model.getRecyclerViewLayoutManager(this@Main).onSaveInstanceState()
        )
        outState.putParcelable("currentPhrase", model.currentPhrase)
        outState.putParcelableArrayList("array", model.adapter.getAll())
        outState.putSerializable("currentGameState", model.currentGameState)
        outState.putParcelable("graphics", graphics)
        outState.putInt("backgroundMediaId", model.adapter.backgroundMediaId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.getRecyclerViewLayoutManager(this)
            .onRestoreInstanceState(savedInstanceState.getParcelable("recyclerview"))

        graphics = savedInstanceState.getParcelable("graphics")!!
        model.currentGameState =
            savedInstanceState.getSerializable("currentGameState") as MainModel.GameState

        model.symbols = savedInstanceState.getParcelable<Parcelable>("symbols") as TableOfSymbols
        model.adapter.setArray(savedInstanceState.getParcelableArrayList("array") ?: ArrayList())
        model.currentPhrase =
            savedInstanceState.getParcelable<Parcelable>("currentPhrase") as Phrase
        model.adapter.backgroundMediaId = savedInstanceState.getInt("backgroundMediaId")
    }

    override fun onStart() {
        Std.debug("[STATE] Main : onStart")
        super.onStart()
        model.registerListener(
            this, R.id.main_button_continue,
            ::onContinueButtonPressed
        )
        model.registerListener(this, R.id.main_button, ::onMainButtonPressed)
        model.registerListener(this, R.id.mainactivity_choicebox_area, ::onChoiceAreaPressed)
    }

    override fun onBackPressed() {
        Std.debug("[STATE] Main : onBackPressed")
        Std.confirm(
            getString(R.string.quit_conv),
            getString(R.string.yes),
            getString(R.string.no),
            {
                super.onBackPressed()
            }, {}, this
        )
    }

    override fun onResume() {
        super.onResume()
        Std.debug("[STATE] Main : onResume")
        model.sound.resume()
        model.sound2.resume()
        play()
    }

    override fun onDestroy() {
        model.sound2.stop()
        model.sound2.onDestroy()
        model.sound.stop()
        model.sound.onDestroy()
        super.onDestroy()
    }


    override fun onPause() {
        Std.debug("[STATE] Main : onPause")
        model.sound.pause()
        model.sound2.pause()
        pause()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Std.debug("[STATE] Main : onWindowFocusChanged")
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && model.isFirstStart()) {
            MainGraphics.fadeBlackFilter(this)
            graphics()
        }
        if (hasFocus && graphics.videoToReload != "") {
            switchVideo(graphics.videoToReload)
        }
    }

    /**
     * Plays
     */
    private fun play() {
        if (model.currentGameState !== MainModel.GameState.PAUSED
            && model.currentGameState !== MainModel.GameState.USER_PAUSED
        ) {
            return
        }
        Std.debug("[STATE] Main : playing the game")

        model.mh.kill()
        model.adapter.removeIfLastIs(Phrase.Type.typing)
        discuss(model.currentPhrase)
    }

    /**
     * Pauses the game
     * @param isUserPaused : Boolean
     */
    private fun pause(isUserPaused: Boolean = false) {
        if (model.currentGameState !== MainModel.GameState.PLAYING) {
            return
        }
        Std.debug("[STATE] Main : pausing the game")
        model.mh.kill()
        model.adapter.removeIfLastIs(Phrase.Type.typing)
        model.currentGameState =
            (if (isUserPaused) MainModel.GameState.USER_PAUSED else MainModel.GameState.PAUSED)
    }

    private fun load(symbols: TableOfSymbols) {
        val glide = Glide.with(this)
        val characters = TableOfCharacters(symbols.chapterCode, symbols)
        val adapter = GameConversationAdapter(
            this,
            ArrayList(),
            characters,
            glide,
            symbols.chapterCode == "7a"
        )

        model = MainModel(this, symbols, glide, adapter)
        model.logEvent()
        graphics = MainGraphics(
            model.getChapterConversationName(this),
            model.getChapterConversationStatus(this),
            model.getChapterStartingProfilPicture(this),
            0,
            choiceBoxIsVisible = false,
            mainButtonIsVisible = false,
            lostImageIsVisible = false,
            iconsAreVisible = true,
            isDescriptionVisible = true
        )
        graphics.setRecyclerView(this, model.adapter, windowManager.defaultDisplay)
    }

    /**
     * Sets initial graphics settings
     */
    private fun graphics() {
        val type = MainModel.getGameType(model.symbols.chapterCode)
        graphics.iconsVisibility(this, !model.isNoSeen)
        graphics.setInitialImages(this, model.requestManager, type)
        graphics.setDescriptionTitle(this, model.getChapterTitle(this))
        graphics.setDescriptionContent(this, model.getChapterContent(this))
        graphics.setProfilPicture(this, model.requestManager)
        graphics.setConversationName(this)
        graphics.setConversationStatus(this)
        graphics.setDescriptionVisibility(this)
        graphics.setButtonVisibility(this)
        graphics.switchBackgroundImage(this, model.requestManager)
        if (graphics.choiceBoxIsVisible) {
            onMainButtonPressed()
        }
    }

    /**
     * Fired when the button "continue" is pressed
     */
    private fun onContinueButtonPressed() {
        Std.debug("[STATE] Main : onContinueButtonPressed")
        graphics.fadeDescriptionFilter(this, false)
        when {
            Data.testCurrentChapterPhrases -> testChapterPhrases()
            Data.gameDebugModeEnabled -> console()
            else -> discuss(model.currentPhrase)
        }
    }

    /**
     * Fired when the main button is pressed
     */
    private fun onMainButtonPressed() {
        Std.debug("[STATE] Main : onMainButtonPressed")
        ChoicesController.clear(graphics.getChoiceParentView(this))
        ChoicesController.choose(
            this@Main,
            graphics.getChoiceParentView(this@Main),
            model.getChoices(model.currentPhrase),
            this,
            model.symbols
        )
        graphics.setChoiceBoxVisibility(this, true)
    }

    /**
     * Fired when the user touches the choice area's background
     */
    private fun onChoiceAreaPressed() {
        Std.debug("[STATE] Main : onChoiceAreaPressed")
        graphics.setChoiceBoxVisibility(this, false)
    }

    /**
     * Fired when the user click on a choice
     * @param p : Phrase
     */
    override fun onClickChoice(p: Phrase) {
        graphics.setChoiceBoxVisibility(this, false)
        graphics.setButtonVisibility(this, false)
        var tmp = p
        do {
            tmp.sentence = tmp.withoutInfo()
            insert(tmp, Phrase.Type.me)
            val list = model.links.getDest(tmp.id)
            if (list.isEmpty()) {
                return
            }
            val nextId = list[0]
            tmp = model.phrases.getPhrase(nextId)
        } while (tmp.id_author == 0)

        val ftmp = tmp
        model.currentPhrase = ftmp

        if (!model.isNoSeen) {
            val runnable = object : Runnable2(
                "meSeen", if (SutokoSharedElementsData.IS_FAST_CHAPTER) {
                    0
                } else {
                    Random.nextInt(600, 2000)
                }
            ) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    model.setLastSeenIf(Phrase.Type.me)
                    graphics.scroll(this@Main, model.adapter.itemCount - 1)
                    discuss(ftmp)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
        } else {
            discuss(ftmp)
        }
    }

    override fun onInsertPhrase(position: Int, isSmoothScroll: Boolean) {

    }

    /**
     * Inserts a phrase in the Adapter
     * @param phrase : Phrase to insert
     * @param type : Phrase.Type to assign
     */
    private fun insert(phrase: Phrase, type: Phrase.Type = phrase.getType()) {
        phrase.type = Phrase.determineTypeCode(type)
        val position = model.adapter.insert(phrase)
        graphics.scroll(this, position)
    }

    private fun console() {
        insert(Phrase.fast(1, Phrase.Type.typing, "d", 0, 0))
        insert(Phrase.fast(1, Phrase.Type.dest, "Hello, comment ça va ?", 0, 0))
        insert(Phrase.fast(0, Phrase.Type.me, "Hello, ça va && toi?", 0, 0))
        insert(Phrase.fast(0, Phrase.Type.meSeen, "Hello, ça va && toi?", 0, 0))
        insert(Phrase.fast(1, Phrase.Type.image, "[lac1.png]", 0, 0))
        insert(Phrase.fast(0, Phrase.Type.meImage, "123", 0, 0))
        insert(Phrase.fast(-1, Phrase.Type.info, "Voici une information à afficher", 0, 0))
        insert(Phrase.fast(-1, Phrase.Type.nextChapter, "Voici une information à afficher", 0, 0))
    }

    /**
     * Discusses forward
     * @param p : Phrase
     */
    private fun discussForward(p: Phrase, isTest: Boolean = false) {
        if (isTest) {
            return
        }

        if (model.isUserChoice(p.id)) {
            model.currentGameState = MainModel.GameState.WAITING_FOR_USER
            graphics.setButtonVisibility(this, true)
            return
        }

        if (model.hasAnswer(p)) {
            val next = model.getAnswer(p)
            discuss(next)
        }
    }

    /**
     * Discusses
     * @param p : Phrase
     */
    private fun discuss(p: Phrase, isFast: Boolean = false, isTest: Boolean = false) {
        model.currentPhrase = p
        model.currentGameState = MainModel.GameState.PLAYING

        if (isFast || SutokoSharedElementsData.IS_FAST_CHAPTER) {
            p.seen = 0
            p.wait = 0
        }

        if (DiscussionHandler.execute("Phrase should be skipped", p.needsSkip())) {
            discussForward(p, isTest)
            return
        }
        if (DiscussionHandler.execute("Affichage d'une information", p.`is`(Phrase.Type.info))) {
            val runnable = object : Runnable2("Affichage d'une information", p.seen) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    p.sentence = Character.updateNames(this@Main, p.sentence)
                    p.formatName(model.symbols)
                    insert(p, Phrase.Type.info)
                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute(
                "L'utilisateur envoie un screenshot au père d'Eva",
                p.isScreenShot()
            )
        ) {
            insert(p, Phrase.Type.meImage)
            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute(
                "Verification d'une condition",
                p.`is`(Phrase.Type.condition)
            )
        ) {
            val values = p.answerCondition
            val condition =
                values[0]!!.replace("[", "").replace("]", "").replace(" ", "").split("==".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            val mThen = Integer.parseInt(values[1]!!)
            val mElse = Integer.parseInt(values[2]!!)

            val v = Var(condition[0], condition[1], model.symbols.chapterNumber)
            val next: Phrase
            if (model.symbols.condition(GlobalData.Game.FRIENDZONE.id, v.name, v.value)) {
                next = model.phrases.getPhrase(mThen)
            } else {
                next = model.phrases.getPhrase(mElse)
            }
            model.currentPhrase = next

            if (isTest) {
                return
            }
            discuss(next)
            return
        }


        if (DiscussionHandler.execute(
                "Insertion d'une variable en mémoire",
                p.`is`(Phrase.Type.memory)
            )
        ) {
            val v = p.getVarFromCondition(model.symbols.chapterNumber)

            model.symbols.addOrSet(GlobalData.Game.FRIENDZONE.id, v.name, v.value)

            discussForward(p, isTest)
            return
        }


        if (DiscussionHandler.execute("Un personnage envoie une image", p.isContentImage)) {

            val runnable =
                object : Runnable2("Un personnage envoie une image", p.getSeenWithLangControl()) {
                    override fun run() {
                        if (isFinishing) {
                            return
                        }
                        insert(p, Phrase.Type.image)

                        discussForward(p, isTest)
                    }
                }
            model.mh.push(runnable)
            model.mh.run(runnable)

            return
        }


        if (DiscussionHandler.execute("Le joueur débloque un trophée", p.isTrophy)) {

            val runnable = object : Runnable2("Le joueur débloque un trophée", 1280) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    discussForward(p, isTest)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)

            return
        }

        if (DiscussionHandler.execute("Le joueur a perdu", p.isLost())) {
            val runnable = object : Runnable2("Le joueur a perdu", p.seen) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    graphics.setLostImageVisible(this@Main, true)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Demande de changement d'image de Zoé", p.isZoeSetImage(1))) {
            val runnable = object : Runnable2("Demande de changement d'image de Zoé", 2000) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    model.symbols.addOrSet(GlobalData.Game.FRIENDZONE.id, "zoepp", "1")
                    graphics.setProfilPicture(
                        this@Main,
                        model.requestManager,
                        R.drawable.zoe1_profil
                    )
                    model.adapter.reload()

                    discussForward(p, isTest)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Demande de changement d'image de Zoé", p.isZoeSetImage(2))) {
            val runnable = object : Runnable2("Demande de changement d'image de Zoé", 2000) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    model.symbols.addOrSet(GlobalData.Game.FRIENDZONE.id, "zoepp", "2")
                    graphics.setProfilPicture(
                        this@Main,
                        model.requestManager,
                        R.drawable.zoe2_profil
                    )
                    model.adapter.reload()

                    discussForward(p, isTest)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute(
                "Zoé vous a ajouté à sa liste d'amis",
                p.sentence == "[FRIEND/Zoé Topaze/00:19]"
            )
        ) {
            graphics.fillNotification(
                this,
                model.requestManager,
                getString(R.string.alias_zoe_topaze),
                getString(R.string.added_you_to_her_friendlist),
                R.drawable.zoe
            )
            val duration = graphics.notification(this, true)
            val runnable =
                object : Runnable2("Zoé a accepté votre demande en ami", duration + 2000) {
                    override fun run() {
                        if (isFinishing) {
                            return
                        }
                        graphics.setUserStatus(this@Main, true)
                        graphics.notification(this@Main, false)

                        discussForward(p, isTest)
                    }
                }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }


        if (DiscussionHandler.execute(
                "Eva a accepté votre demande en ami",
                p.sentence == "[ACCEPT/Eva Belle/16:05]"
            )
        ) {
            graphics.fillNotification(
                this,
                model.requestManager,
                getString(R.string.alias_eva_belle),
                Character.updateNames(this@Main, getString(R.string.accepted_chat_request)),
                R.drawable.eva
            )
            val runnable = object : Runnable2("Eva a accepté votre demande en ami A", p.seen) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    val duration = graphics.notification(this@Main, true)
                    model.playSound(
                        this@Main,
                        "notification",
                        false,
                        MainModel.SoundType.FOREGROUND
                    )
                    val runnable = object :
                        Runnable2("Eva a accepté votre demande en ami B", duration + 3000) {
                        override fun run() {
                            graphics.setUserStatus(this@Main, true)
                            graphics.notification(this@Main, false)

                            discussForward(p, isTest)
                        }
                    }
                    model.mh.push(runnable)
                    model.mh.run(runnable)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }


        if (DiscussionHandler.execute("Un personnage s'est déconnecté", p.isOffline())) {
            graphics.setUserStatus(this, false)

            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Un personnage a banni le joueur", p.isBanned())) {
            insert(Phrase.fast(-1, Phrase.Type.info, getString(R.string.mainactivity_banned), 0, 0))
            graphics.setUserStatus(this, false)

            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Lancement d'un son", p.isSound)) {
            val runnable = object : Runnable2("Demande de notification overlay + ", p.seen) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    model.playSound(this@Main, p.soundName, false, MainModel.SoundType.BACKGROUND)

                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Lancement du jeu contre Bryan", p.isSaveZoeGame())) {
            model.currentPhrase = model.getAnswer(p)
            val i = Intent(this@Main, Game::class.java)
            i.putExtra("symbols", model.symbols as Parcelable)
            startActivity(i)
            return
        }

        if (DiscussionHandler.execute("Glitch animation", p.isGlitchAnimation())) {
            model.playSound(this, "glitch", false, MainModel.SoundType.BACKGROUND)
            model.adapter.backgroundMediaId = R.drawable.glitch4
            graphics.glitchAnimation(this, p.seen, model.mh)

            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Lancement d'une vidéo", p.isMp4())) {
            switchVideo(p.getMp4())

            discussForward(p, isTest)
            return
        }


        if (DiscussionHandler.execute("Affichage d'une image en fond", p.isBackgroundImage)) {
            val runnable = object : Runnable2("Demande de changement d'image de fond ", p.seen) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    val id = graphics.switchBackgroundImage(
                        this@Main,
                        model.requestManager,
                        Std.getResourceIdFromName(this@Main, p.backgroundImageName, "drawable", -1)
                    )
                    model.adapter.backgroundMediaId = id

                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Lancement d'un nouveau chapitre", p.isNextChapter)) {
            @Suppress("ConstantConditionIf")
            if (Data.testCurrentChapterPhrases) {
                return
            }

            val chapter = p.nextChapter
            insert(p, Phrase.Type.nextChapter)
            val runnable = object : Runnable2("Nouveau chapitre", Phrase.nextChapterDelay()) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    val duration = Animation.setAnimation(
                        findViewById(R.id.main_main_filter_black),
                        Animation.Animations.ANIMATION_FADEIN,
                        this@Main
                    ).toInt()
                    val runnable = object : Runnable2("fadeIn black filter", duration) {
                        override fun run() {
                            if (isFinishing) {
                                return
                            }
                            model.symbols.addOrSet(
                                GlobalData.Game.FRIENDZONE.id,
                                "chapterCode",
                                chapter
                            )
                            model.symbols.save(this@Main)
                            setResult(
                                RESULT_OK, Intent()
                                    .putExtra("symbols", model.symbols as Parcelable)
                            )
                            finish()
                        }
                    }
                    model.mh.push(runnable)
                    model.mh.run(runnable)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        DiscussionHandler.execute("Eva parle", Runnable {
            p.sentence = Character.updateNames(this@Main, p.sentence)
            p.formatName(model.symbols)
            if (p.seen == 0 && p.wait == 0) {
                insert(p, Phrase.Type.dest)
                discussForward(p, isTest)
                return@Runnable
            }

            val runnable = object : Runnable2("Eva parle", p.getSeenWithLangControl()) {
                override fun run() {
                    if (isFinishing) {
                        return
                    }
                    if (p.wait > 0 && !model.isNoSeen) {
                        insert(p, Phrase.Type.typing)
                        model.playSound(this@Main, "typing", false, MainModel.SoundType.FOREGROUND)
                    }

                    val runnable = object : Runnable2("Typing", p.getWaitWithLangControl()) {
                        override fun run() {
                            if (p.wait > 0 && !model.isNoSeen) {
                                model.adapter.editLast(p, Phrase.Type.dest)
                            } else {
                                insert(p, Phrase.Type.dest)
                            }
                            if (!model.isNoSeen) {
                                model.playSound(
                                    this@Main,
                                    "message",
                                    false,
                                    MainModel.SoundType.FOREGROUND
                                )
                            }
                            discussForward(p, isTest)
                        }
                    }
                    model.mh.push(runnable)
                    model.mh.run(runnable)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
        })
    }


    /**
     * Switch the background to the video mode
     *
     * @param name the name of the resource without the extension.
     */
    private fun switchVideo(name: String) {
        graphics.videoToReload = name
        val r = findViewById<View>(R.id.mainactivity_background)
        val v = findViewById<VideoView>(R.id.mainactivty_background_video)
        val i = findViewById<View>(R.id.mainactivty_background_image)
        model.hasBackgroundMedia = true
        val id = Video.determine(name, this)
        model.adapter.backgroundMediaId = id
        Video.put(
            v,
            Uri.parse("android.resource://$packageName/$id"),
            true
        ) {
            graphics.videoToReload = ""
            model.adapter.backgroundMediaId = 0
        }

        Animation.setAnimation(r, Animation.Animations.ANIMATION_FADEIN, this)
        r.visibility = View.VISIBLE
        Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEIN, this)
        v.visibility = View.VISIBLE
        Animation.setAnimation(i, Animation.Animations.ANIMATION_FADEOUT, this)
        i.visibility = View.INVISIBLE
    }


    /**
     * Testes all array of the current chapter
     * @see fr.purpletear.friendzone.tables.TableOfPhrases
     */
    private fun testChapterPhrases() {
        model.phrases.array.forEach {
            discuss(it, isFast = true, isTest = true)
        }
    }
}
