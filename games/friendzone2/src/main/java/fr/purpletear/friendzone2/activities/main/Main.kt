/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sharedelements.OnlineAssetsManager
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.friendzone2.Data
import fr.purpletear.friendzone2.activities.choice.Choice
import fr.purpletear.friendzone2.activities.phone.homescreen.HomeScreen
import fr.purpletear.friendzone2.configs.*
import fr.purpletear.friendzone2.tables.Character
import fr.purpletear.friendzone2.tables.TableOfCharacters
import kotlin.random.Random
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.configs.Var
import fr.purpletear.friendzone2.tables.Language
import purpletear.fr.purpleteartools.*
import purpletear.fr.purpleteartools.TableOfSymbols
import fr.purpletear.friendzone2.R


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

    private var killDiscussion : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Std.debug("[STATE] Main : onCreate")
        super.onCreate(savedInstanceState)
        setContentView(MainModel.getGameLayout())
        load()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("symbols", model.symbols)
        outState.putParcelable("recyclerview", model.getRecyclerViewLayoutManager(this@Main).onSaveInstanceState())
        outState.putParcelable("currentPhrase", model.currentPhrase)
        outState.putParcelableArrayList("array", model.adapter.getAll())
        outState.putSerializable("currentGameState", model.currentGameState)
        outState.putParcelable("graphics", graphics)
        outState.putString("backgroundMediaId", model.adapter.backgroundMediaId as String)
        outState.putBoolean("isNightMode", model.adapter.isNightMode)
        outState.putBoolean("killDiscussion", killDiscussion)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.getRecyclerViewLayoutManager(this).onRestoreInstanceState(savedInstanceState.getParcelable("recyclerview"))

        graphics = savedInstanceState.getParcelable("graphics")!!
        model.currentGameState = savedInstanceState.getSerializable("currentGameState") as MainModel.GameState

        model.symbols = savedInstanceState.getParcelable<Parcelable>("symbols") as TableOfSymbols
        model.adapter.setArray(savedInstanceState.getParcelableArrayList("array") ?: ArrayList())
        model.currentPhrase = savedInstanceState.getParcelable<Parcelable>("currentPhrase") as Phrase
        model.adapter.backgroundMediaId = savedInstanceState.getString("backgroundMediaId") ?: ""
        model.adapter.isNightMode = savedInstanceState.getBoolean("model.adapter.isNightMode", false)
        killDiscussion = savedInstanceState.getBoolean("killDiscussion", false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }

        if(requestCode == model.choiceRequestCode) {
            if (data != null && data.hasExtra("symbols")) {
                model.symbols = (data.getParcelableExtra<Parcelable>("symbols") as TableOfSymbols)
            }
        }
    }

    override fun onStart() {
        Std.debug("[STATE] Main : onStart")
        super.onStart()
        model.registerListener(this, R.id.main_button_continue,
                ::onContinueButtonPressed)
        model.registerListener(this, R.id.main_button, ::onMainButtonPressed)
        model.registerListener(this, R.id.mainactivity_choicebox_area, ::onChoiceAreaPressed)
    }

    override fun onBackPressed() {
        Std.debug("[STATE] Main : onBackPressed")
        Std.confirm(
                Character.updateNames(this, getString(R.string.quit_conv)),
                getString(R.string.yes),
                getString(R.string.no),
                {
                    super.onBackPressed()
                }, {} ,this
        )
    }

    override fun onResume() {
        Std.debug("[STATE] Main : onResume")
        play()
        super.onResume()
    }

    override fun onDestroy() {
        Std.debug("[STATE] Main : onDestroy")
        super.onDestroy()
    }

    override fun onPause() {
        Std.debug("[STATE] Main : onPause")
        model.sh.clear()
        pauseSinglePlayer()
        pause()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Std.debug("[STATE] Main : onWindowFocusChanged")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics()
            manageButtons()
        }
        if (hasFocus && graphics.videoToReload != "") {
            switchVideo(graphics.videoToReload)
        }
        graphics.fadeBlackFilter(this)
    }

    /**
     * Testes all array of the current chapter
     * @see fr.purpletear.friendzone2.tables.TableOfPhrases
     */
    private fun testChapterPhrases() {
        model.phrases.array.forEach {
            discuss(it, isFast = true, isTest = true)
        }
    }

    private fun play() {
        if (model.currentGameState !== MainModel.GameState.PAUSED
                && model.currentGameState !== MainModel.GameState.USER_PAUSED
                || Data.testCurrentChapterPhrases) {
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
        if (model.currentGameState !== MainModel.GameState.PLAYING
                || Data.testCurrentChapterPhrases) {
            return
        }
        Std.debug("[STATE] Main : pausing the game")
        model.mh.kill()
        model.adapter.removeIfLastIs(Phrase.Type.typing)
        model.adapter.removeIfLastIs(Phrase.Type.meTyping)
        model.currentGameState = (if (isUserPaused) MainModel.GameState.USER_PAUSED else MainModel.GameState.PAUSED)
    }

    private fun load() {
        val glide = Glide.with(this)
        val symbols = intent.getParcelableExtra<TableOfSymbols>("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)
        val characters = TableOfCharacters(this, symbols.chapterCode, symbols)
        val adapter = GameConversationAdapter(this, ArrayList(), characters, glide, this@Main)

        model = MainModel(this, symbols, glide, adapter)
        graphics = MainGraphics(
                model.getChapterConversationName(this),
                model.getChapterConversationStatus(this),
                model.getChapterStartingProfilPicture(this),
                "",
                OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_torch_disabled"),
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
        graphics.setTorchVisibility(this, model.shouldDisplayTorch())
        graphics.iconsVisibility(this, !model.isNoSeen)
        graphics.setInitialImages(this, model.requestManager)
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
    @Suppress("ConstantConditionIf")
    private fun onContinueButtonPressed() {
        Std.debug("[STATE] Main : onContinueButtonPressed")
        Finger.registerListener(this@Main, R.id.main_button_continue) {
            // Keep it empty.
        }
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
        fillChoiceBox()
    }

    /**
     * Files and open the choice box
     * @param phrase : Phrase
     */
    private fun fillChoiceBox(phrase : Phrase = model.currentPhrase) {
        ChoicesController.clear(graphics.getChoiceParentView(this))
        ChoicesController.choose(
                this@Main,
                graphics.getChoiceParentView(this@Main),
                model.getChoices(phrase),
                this,
                model.symbols)
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
        if(killDiscussion) {
            killDiscussion = false
            model.mh.kill()
        }
        graphics.setChoiceBoxVisibility(this, false)
        graphics.setButtonVisibility(this, false)
        var tmp = p
        do {
            tmp.sentence = tmp.withoutInfo()
            insert(tmp, Phrase.Type.me)
            val nextId = model.links.getDest(tmp.id)[0]
            tmp = model.phrases.getPhrase(nextId)
        } while (tmp.id_author == 0)

        val ftmp = tmp
        model.currentPhrase = ftmp

        if (!model.isNoSeen) {
            val runnable = object : Runnable2("meSeen", if (SutokoSharedElementsData.IS_FAST_CHAPTER) {
                0
            } else {
                Random.nextInt(600, 2000)
            }) {
                override fun run() {
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

    override fun onClickSound(name: String) {
        model.adapter.currentPlayingSound = model.singlePlayer.playWithNameFromFullPath(OnlineAssetsManager.getSoundFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), name), this@Main) {
            pauseSinglePlayer()
        }
        model.adapter.notifyDataSetChanged()
    }

    /**
     * Pauses single player
     */
    private fun pauseSinglePlayer() {
        model.singlePlayer.kill()
        model.adapter.currentPlayingSound = ""
        model.adapter.notifyDataSetChanged()
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
        insert(Phrase.fast(1, Phrase.Type.image, "[phone.png]", 0, 0))
        insert(Phrase.fast(-1, Phrase.Type.info, "Voici une information à afficher", 0, 0))
        insert(Phrase.fast(-1, Phrase.Type.nextChapter, "Voici une information à afficher", 0, 0))
    }

    /**
     * Discusses forward
     * @param p : Phrase
     */
    private fun discussForward(p: Phrase, isTest: Boolean = false) {
        if(isTest) {
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
     * Moves forward with the currentPhrase
     * @param p : Phrase src
     */
    private fun moveForward(p : Phrase) {
        if(model.hasAnswer(p)) {
            model.currentPhrase = model.getAnswer(p)
        }
    }

    /**
     * Returns the sentence formated with the symbols
     * @param p : Phrase
     * @return String
     */
    private fun updateSymbolesInSentences(p : Phrase) : String {
        for(s in model.symbols.getArray(GlobalData.Game.FRIENDZONE2.id)) {
            p.sentence = p.sentence.replace("[${s.n}]", s.v)
        }
        return p.sentence
    }

    /**
     * Discusses
     * @param p : Phrase
     */
    private fun discuss(p: Phrase, isFast: Boolean = false, isTest: Boolean = false) {
        model.currentPhrase = p
        p.formatName(model.symbols)
        p.toEmojis()
        p.sentence = updateSymbolesInSentences(p)


        model.currentGameState = MainModel.GameState.PLAYING

        if (isFast ||
                (SutokoSharedElementsData.IS_FAST_CHAPTER
                        && model.symbols.chapterNumber != 1
                        && model.symbols.chapterNumber != 4
                        )) {
            if(p.getType() != Phrase.Type.condition) {
                p.seen = 0
            }
            p.wait = 0
        }

        if (DiscussionHandler.execute("Phrase should be skipped", p.needsSkip())) {
            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Affichage d'une information", p.`is`(Phrase.Type.info))) {
            val runnable = object : Runnable2("Affichage d'une information", p.seen) {
                override fun run() {
                    p.sentence = Character.updateNames(this@Main, p.sentence)
                    insert(p, Phrase.Type.info)
                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Verification d'une condition", p.`is`(Phrase.Type.condition))) {
            val values = p.answerCondition
            val condition = values[0]!!.replace("[", "").replace("]", "").replace(" ", "").split("==".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val mThen = Integer.parseInt(values[1]!!)
            val mElse = Integer.parseInt(values[2]!!)

            val v = Var(condition[0], condition[1], model.symbols.chapterNumber)
            val next: Phrase
            if (model.symbols.condition(GlobalData.Game.FRIENDZONE2.id, v.name, v.value)) {
                next = model.phrases.getPhrase(mThen)
            } else {
                next = model.phrases.getPhrase(mElse)
            }
            model.currentPhrase = next

            if(isTest) {
                return
            }
            discuss(next)
            return
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", p.isTrophy)) {

            val runnable = object : Runnable2("Le joueur débloque un trophée", 1280) {
                override fun run() {
                    discussForward(p, isTest)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)

            return
        }


        if (DiscussionHandler.execute("Insertion d'une variable en mémoire", p.`is`(Phrase.Type.memory))) {
            val v = p.getVarFromCondition(model.symbols.chapterNumber)

            model.symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id, v.name, v.value)

            discussForward(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Insertion d'un information No Signal", p.isNoSignal())) {
            model.adapter.insert(p, Phrase.Type.noSignal)
            discussForward(p, isTest)
            return
        }


        if (DiscussionHandler.execute("Un personnage envoie une image", p.isContentImage)) {

            val runnable = object : Runnable2("Un personnage envoie une image", p.seen) {
                override fun run() {
                    insert(p, Phrase.Type.image)
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
                    graphics.setLostImageVisible(this@Main, true)
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
                    model.playSound(this@Main, OnlineAssetsManager.getSoundFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), p.soundName), false)
                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
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
                    val id = graphics.switchBackgroundImage(this@Main, model.requestManager, OnlineAssetsManager.getImageFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), p.backgroundImageName))
                    model.adapter.backgroundMediaId = id
                    discussForward(p, isTest)
                }
            }

            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if (DiscussionHandler.execute("Chloé vous a ajouté à sa liste d'amis", p.sentence == "[FRIEND/Chloé Winsplit/21:30]")) {
            graphics.fillNotification(this, model.requestManager, getString(R.string.alias_chloe_winsplit), getString(R.string.added_you_to_her_friendlist),
                OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "chloe"),
                OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_add_friend"))
            val duration = graphics.notification(this, true)
            val runnable = object : Runnable2("Chloé a accepté votre demande en ami", duration + 2000) {
                override fun run() {
                    graphics.setUserStatus(this@Main, true)
                    graphics.notification(this@Main, false)
                    discussForward(p, isTest)
                }
            }
            model.mh.push(runnable)
            model.mh.run(runnable)
            return
        }

        if(DiscussionHandler.execute("Gestion d'un code", p.isCode())) {
            manageCode(p, isTest)
            return
        }

        if (DiscussionHandler.execute("Lancement d'un nouveau chapitre", p.isNextChapter)) {
            @Suppress("ConstantConditionIf")
            if(Data.testCurrentChapterPhrases) {
                return
            }

            val chapter = p.nextChapter
            insert(p, Phrase.Type.nextChapter)
            val runnable = object : Runnable2("Nouveau chapitre", Phrase.nextChapterDelay()) {
                override fun run() {
                    val duration = Animation.setAnimation(
                            findViewById(R.id.main_main_filter_black),
                            Animation.Animations.ANIMATION_FADEIN,
                            this@Main
                    ).toInt()
                    val runnable = object : Runnable2("fadeIn black filter", duration) {
                        override fun run() {
                            model.symbols.chapterCode = chapter
                            model.symbols.save(this@Main)
                            setResult(RESULT_OK, Intent()
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
            if (p.seen == 0 && p.wait == 0) {
                insert(p, if(model.isSpectatorMode(p)){Phrase.Type.me} else {Phrase.Type.dest})
                discussForward(p, isTest)
                return@Runnable
            }

            val runnable = object : Runnable2("Eva parle", p.seen) {
                override fun run() {
                    if (p.wait > 0 && !model.isNoSeen) {
                        insert(p, if(model.isSpectatorMode(p)){Phrase.Type.meTyping} else {Phrase.Type.typing})
                        model.playSound(this@Main, OnlineAssetsManager.getSoundFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), "typing"), false)
                    }

                    val runnable = object : Runnable2(OnlineAssetsManager.getSoundFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), "typing"), p.wait) {
                        override fun run() {
                            if (p.wait > 0 && !model.isNoSeen) {
                                model.adapter.editLast(p, if(model.isSpectatorMode(p)){Phrase.Type.me} else {Phrase.Type.dest})
                            } else {
                                insert(p, if(model.isSpectatorMode(p)){Phrase.Type.me} else {Phrase.Type.dest})
                            }
                            if (!model.isNoSeen) {
                                model.playSound(this@Main, OnlineAssetsManager.getSoundFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), "message"), false)
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
     * Handles the code
     * @param p : Phrase
     * @param isTest : Boolean
     */
    private fun manageCode(p : Phrase, isTest : Boolean = false) {
        when (p.getCode()) {
            1 -> {
                graphics.setTorchVisibility(this@Main, true)
                graphics.setTorchImage(this@Main, MainModel.TorchState.DISABLED, model.requestManager)
                discussForward(p, isTest)
            }
            3 -> {
                moveForward(p)
                val intent = Intent(this@Main, HomeScreen::class.java)
                intent.putExtra("symbols", model.symbols as Parcelable)
                startActivity(intent)
                return
            }

            4 -> {
                insertZoeVocals()
                discussForward(p, isTest)
            }
            5 -> {
                graphics.fillNotification(this@Main, model.requestManager, getString(R.string.alias_chloe_winsplit), getString(R.string.chloe_notif_libelle_a),
                    OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "chloe"), OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_sms"))
                val duration = graphics.notification(this, true)
                val runnable = object : Runnable2("Chloé vous envoie un sms", duration + 4500) {
                    override fun run() {
                        graphics.notification(this@Main, false)
                        discussForward(p, isTest)
                    }
                }
                model.mh.push(runnable)
                model.mh.run(runnable)
            }

            6 -> {
                moveForward(p)
                val intent = Intent(this@Main, Choice::class.java)
                intent.putExtra("symbols", model.symbols as Parcelable)
                startActivityForResult(intent, model.choiceRequestCode)
                return
            }

            7 -> {
                graphics.stopVideo(this)
                discussForward(p, isTest)
                return
            }

            8 -> {
                val zoeCall = arrayOf("8a", "8b", "8d", "8h")
                if(zoeCall.contains(model.symbols.chapterCode)) {
                    graphics.fillNotification(
                            this, model.requestManager,
                            getString(R.string.alias_zoe_topaze),
                            getString(R.string.is_calling_you),
                        OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "zoe1_profil"), OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_vibrating"))
                } else {
                    graphics.fillNotification(
                            this, model.requestManager,
                            getString(R.string.alias_lucie_belle),
                            getString(R.string.is_calling_you),
                        OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "lucie_profil"), OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_vibrating"))

                }
                val duration = graphics.notification(this, true)
                val runnable = object : Runnable2("Zoé ou Lucie vous appelle", duration + 3725) {
                    override fun run() {
                        graphics.notification(this@Main, false)
                        discussForward(p, isTest)
                    }
                }
                model.mh.push(runnable)
                model.mh.run(runnable)
                model.vibrate(this, 3)
                return
            }

            10 -> {
                graphics.setGunVisibility(this, true)
                model.symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id, "gun", "true")
                discussForward(p, isTest)
                return
            }

            12 -> {
                killDiscussion = true
                graphics.setButtonVisibility(this, true)
                if(model.symbols.condition(GlobalData.Game.FRIENDZONE2.id, "gun", "true")) {
                    graphics.setGunVisibility(this, true)
                    graphics.setGunImage(this, MainModel.GunState.AVAILABLE, model.requestManager)
                }

                Finger.defineOnTouch(
                        findViewById(R.id.main_button),
                        this) {
                    fillChoiceBox(model.phrases.getPhrase(98))
                }

                Finger.defineOnTouch(
                        findViewById(R.id.mainactivity_button_gun),
                        this) {
                    graphics.setButtonVisibility(this, false)
                    model.mh.kill()
                    discuss(model.phrases.getPhrase(99))
                }

                discussForward(p, isTest)
                return
            }

            13 -> {
                graphics.setGunImage(this, MainModel.GunState.DISABLED, model.requestManager)


                Finger.defineOnTouch(
                        findViewById(R.id.mainactivity_button_gun),
                        this) {}
                discussForward(p, isTest)
            }

            15 -> {
                graphics.fillNotification(this@Main, model.requestManager, getString(R.string.alias_chloe_winsplit), getString(R.string.chloe_notif_libelle_b), OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "chloe"), OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_sms"))
                val duration = graphics.notification(this, true)
                val runnable = object : Runnable2("Chloé vous envoie un sms", duration + 4500) {
                    override fun run() {
                        graphics.notification(this@Main, false)
                        discussForward(p, isTest)
                    }
                }
                model.mh.push(runnable)
                model.mh.run(runnable)
            }
        }
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
        model.adapter.backgroundMediaId = name
        Video.put(
                v,
                Uri.parse("android.resource://$packageName/$id"),
                true) {
            graphics.videoToReload = ""
            model.adapter.backgroundMediaId = ""
        }

        Animation.setAnimation(r, Animation.Animations.ANIMATION_FADEIN, this)
        r.visibility = View.VISIBLE
        Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEIN, this)
        v.visibility = View.VISIBLE
        Animation.setAnimation(i, Animation.Animations.ANIMATION_FADEOUT, this)
        i.visibility = View.INVISIBLE
    }

    /**
     * Inserts Zoe's vocal messages
     */
    private fun insertZoeVocals() {
        if (model.symbols.chapterCode == "5a" && model.symbols.condition(GlobalData.Game.FRIENDZONE2.id,"bryanDenounced", "true")) {
            insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_1a.mp3]", 0, 0))
            insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_1c.mp3]", 0, 0))
        } else if (model.symbols.chapterCode == "5a") {
            insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_1a.mp3]", 0, 0))
            insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_1b.mp3]", 0, 0))

        } else if (model.symbols.chapterCode == "5b" && model.symbols.condition(GlobalData.Game.FRIENDZONE2.id,"bryanDenounced", "true")) {
            if(Language.determinCode() == Language.Companion.Code.FR) {
                insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_2_1.mp3]", 0, 0))
            } else {
                insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_2a.mp3]", 0, 0))
                insert(Phrase.fast(50, Phrase.Type.dest, getString(R.string.bryan_denounced_5b), 0, 0))
            }
        } else {
            if(Language.determinCode() == Language.Companion.Code.FR) {
                insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_2_2.mp3]", 0, 0))
            } else {
                insert(Phrase.fast(50, Phrase.Type.vocal, "[${Language.determineLangDirectory().substring(0, 2)}_2a.mp3]", 0, 0))
                insert(Phrase.fast(50, Phrase.Type.dest, getString(R.string.bryan_not_denounced_5b), 0, 0))
            }
        }
    }

    /**
     * Manages apparition of buttons :
     * - Torch light
     * - Gun
     */
    private fun manageButtons() {

        if(killDiscussion) {
            graphics.setButtonVisibility(this, true)
            if(model.symbols.condition(GlobalData.Game.FRIENDZONE2.id,"gun", "true")) {
                graphics.setGunVisibility(this, true)
                graphics.setGunImage(this, MainModel.GunState.AVAILABLE, model.requestManager)
            }

            Finger.defineOnTouch(
                    findViewById(R.id.main_button),
                    this) {
                fillChoiceBox(model.phrases.getPhrase(98))
            }

            Finger.defineOnTouch(
                    findViewById(R.id.mainactivity_button_gun),
                    this) {
                graphics.setButtonVisibility(this, false)
                model.mh.kill()
                discuss(model.phrases.getPhrase(99))
            }
        }

        if(model.symbols.chapterNumber == 1
                || Data.fastButtonAlwaysDisplaying) {
            graphics.setFasterImage(this, model.requestManager, OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_faster"))
            graphics.setFasterVisibility(this, true)
            Finger.registerListener(this, R.id.mainactivity_button_faster_build, ::onFasterPressed)
        }

        when (model.symbols.chapterNumber) {
            1 -> {
                graphics.setPauseButtonVisibility(this, true)
                graphics.setPauseButtonImage(this, model.requestManager, MainModel.PauseButtonState.PLAYING)
                Finger.registerListener(this, R.id.mainactivity_button_pause, ::onPausePressed)
            }

            8 -> {
                graphics.setTorchImage(this, model.requestManager)
                graphics.setTorchVisibility(this, true)
                if (model.weAreInForest()) {
                    graphics.setTorchImage(this, MainModel.TorchState.OFF, model.requestManager)
                    findViewById<View>(R.id.mainactivty_background_filter).visibility = View.VISIBLE
                    model.adapter.isNightMode = true
                    Finger.registerListener(this, R.id.mainactivity_button_torch, ::onTorchPressed)
                }
            }

            9 -> {
                graphics.setTorchVisibility(this, true)
                graphics.setTorchImage(this, MainModel.TorchState.DISABLED, model.requestManager)
            }

            else -> {
                graphics.setTorchVisibility(this, false)
            }
        }
        graphics.setGunVisibility(this, model.symbols.condition(GlobalData.Game.FRIENDZONE2.id,"gun", "true"))
        graphics.setGunImage(this, MainModel.GunState.DISABLED, model.requestManager)
    }

    private fun onTorchPressed() {
        model.adapter.isNightMode = !model.adapter.isNightMode
        if (!model.adapter.isNightMode) {
            graphics.setTorchImage(this, MainModel.TorchState.ON, model.requestManager)
            Animation.setAnimation(findViewById(R.id.mainactivty_background_filter), Animation.Animations.ANIMATION_FADEOUT, this@Main)
            model.symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id, "statetorch", "true")
        } else {
            model.symbols.removeVar(GlobalData.Game.FRIENDZONE2.id,"statetorch")
            graphics.setTorchImage(this, MainModel.TorchState.OFF, model.requestManager)
            Animation.setAnimation(findViewById(R.id.mainactivty_background_filter), Animation.Animations.ANIMATION_FADEIN, this@Main)
        }
        model.adapter.notifyDataSetChanged()
        model.playSound(this, OnlineAssetsManager.getSoundFilePath(this@Main, GlobalData.Game.FRIENDZONE2.id.toString(), "torch"), false)
    }

    private fun onPausePressed() {
        when (model.currentGameState) {
            MainModel.GameState.PAUSED, MainModel.GameState.USER_PAUSED -> {
                play()
                graphics.setPauseButtonImage(this, model.requestManager, MainModel.PauseButtonState.PLAYING)
            }
            MainModel.GameState.PLAYING -> {
                pause(true)
                graphics.setPauseButtonImage(this,  model.requestManager, MainModel.PauseButtonState.PAUSED)
            }
            else -> {

            }
        }
    }

    private fun onFasterPressed() {
        if(model.currentGameState == MainModel.GameState.USER_PAUSED
                || model.currentGameState == MainModel.GameState.WAITING_FOR_USER
                || model.currentGameState == MainModel.GameState.PAUSED) {
            return
        }
        model.mh.kill()
        model.adapter.removeIfLastIs(Phrase.Type.nextChapter)
        model.adapter.removeIfLastIs(Phrase.Type.typing)
        model.adapter.removeIfLastIs(Phrase.Type.meTyping)
        discuss(model.currentPhrase, true)
    }
}
