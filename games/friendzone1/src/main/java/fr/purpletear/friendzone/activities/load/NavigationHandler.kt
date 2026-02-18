/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.load

import android.content.Context
import android.content.Intent
import fr.purpletear.friendzone.activities.main.Main
import fr.purpletear.friendzone.activities.textcinematic.TextCinematic

/**
 * The NavigationHandler handles the destination from the currentActivity.
 */
internal class NavigationHandler {

    /**
     * Determines the destination
     */
    var destination: Navigation? = null
    private set

    /**
     * @return the request code number using ordinals
     */
    val requestCode: Int
        get() = destination!!.ordinal

    enum class Navigation {
        MENU,
        GAME,
        TEXTCINEMATIC,
        VIDEOAD,
        INTERSTITIAL
    }

    init {
        destination = Navigation.GAME
    }

    /**
     * Reloads the destionation
     *
     * @param n Navigation enum
     */
    fun to(n: Navigation) {
        destination = n
    }

    fun toMenu(): Boolean {
        return destination == Navigation.MENU
    }

    /**
     * Returns the destination Intent
     *
     * @param c Context
     * @return Intent
     */
    @Suppress("ConstantConditionIf")
    fun getIntent(c: Context): Intent? {
        val clss = when (destination) {
            Navigation.MENU -> return null
            Navigation.GAME -> Main::class.java
            Navigation.TEXTCINEMATIC -> TextCinematic::class.java
            else -> throw IllegalArgumentException("destination : $destination")
        }

        return Intent(c, clss)
    }
}
