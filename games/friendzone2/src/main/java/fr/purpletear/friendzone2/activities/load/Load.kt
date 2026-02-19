/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.load

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.ChapterDetailsHandler
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

class Load : AppCompatActivity() {

    /**
     * Handles the model settings
     *
     * @see LoadModel
     */
    private lateinit var model: LoadModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        load()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("symbols", model.symbols)
        outState.putBoolean("hasSeenTextCinematic", model.hasSeenTextCinematic)
        outState.putBoolean("isGranted", model.isGranted)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.hasSeenTextCinematic = savedInstanceState.getBoolean("hasSeenTextCinematic")
        model.isGranted = savedInstanceState.getBoolean("isGranted")
        model.symbols = (savedInstanceState.getParcelable<Parcelable>("symbols") as TableOfSymbols)
    }

    override fun onResume() {
        super.onResume()
        navigate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }

        if (requestCode == NavigationHandler.Navigation.TEXTCINEMATIC.ordinal) {
            model.hasSeenTextCinematic = true
            if (data != null && data.hasExtra("symbols")) {
                model.symbols = (data.getParcelableExtra<Parcelable>("symbols") as TableOfSymbols)
            }
            if (data != null && data.hasExtra("soundPosition")) {
                model.soundPosition = data.getIntExtra("soundPosition", 0)
            }
        } else if (requestCode == NavigationHandler.Navigation.POETRY.ordinal) {
            model.hasSeenPoetry = true
        } else if (requestCode == NavigationHandler.Navigation.GAME.ordinal) {
            model.invalidate()
            if (data == null) {
                return
            }
            model.symbols = (data.getParcelableExtra<Parcelable>("symbols") as TableOfSymbols)
        }
    }

    /**
     * Loads the Activity's vars
     */
    private fun load() {
        val symbols =
            intent.getParcelableExtra("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)

        if (SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) {
            symbols.chapterCode = SutokoSharedElementsData.FORCE_CHAPTER_CODE
            symbols.save(this)
        }

        model = LoadModel(
            symbols, intent.getBooleanExtra("granted", false)
        )
    }

    @Throws
    private fun navigate() {

        model.navigationHandler.to(model.require())

        val i = model.navigationHandler.getIntent(this)

        if (i === null) {
            return
        }

        i.putExtra("symbols", model.symbols as Parcelable)
        i.putExtra(
            "chapter",
            ChapterDetailsHandler.getChapter(this, model.symbols.chapterCode, model.symbols)
        )

        startActivityForResult(
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION),
            model.navigationHandler.requestCode
        )
        overridePendingTransition(0, 0)
    }
}
