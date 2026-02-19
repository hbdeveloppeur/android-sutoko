/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.load

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.ChapterDetailsHandler
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols

class Load : AppCompatActivity() {
    /**
     * Handles the model settings
     *
     * @see LoadModel
     */
    private lateinit var model: LoadModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Std.debug("Load:onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friendzone1_activity_load)
        load()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("symbols", model.symbols)
        outState.putBoolean("granted", model.granted)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        model.granted = savedInstanceState.getBoolean("granted")
        model.symbols =
            (savedInstanceState.getParcelable<TableOfSymbols>("symbols") as TableOfSymbols)
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
            if (data != null && data.hasExtra("symbols")) {
                model.symbols = (data.getParcelableExtra<Parcelable>("symbols") as TableOfSymbols)
            }
        } else if (requestCode == NavigationHandler.Navigation.GAME.ordinal) {
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
            intent.getParcelableExtra("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE.id)
        symbols.gameId = GlobalData.Game.FRIENDZONE.id
        symbols.read(this)

        if (SutokoSharedElementsData.SHOULD_FORCE_CHAPTER) {
            symbols.chapterCode = SutokoSharedElementsData.FORCE_CHAPTER_CODE
        }

        model = LoadModel(
            symbols,
            intent.getBooleanExtra("granted", false)
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
        i.putExtra("chapter", ChapterDetailsHandler.getChapter(this, model.symbols.chapterCode))

        startActivityForResult(
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION),
            model.navigationHandler.requestCode
        )
        overridePendingTransition(0, 0)
    }
}
