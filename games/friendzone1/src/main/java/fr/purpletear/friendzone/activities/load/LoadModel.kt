/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.load

import purpletear.fr.purpleteartools.TableOfSymbols

internal class LoadModel(
    var symbols: TableOfSymbols, var granted : Boolean) {

    /**
     * Determines where to navigate to
     */
    /**
     * Returns the NavigationHandler
     *
     * @return NavigationHandler
     * @see NavigationHandler
     */
    val navigationHandler: NavigationHandler

    init {
        navigationHandler = NavigationHandler()
    }

    fun  require(): NavigationHandler.Navigation {
        return when {
            requiresEnd() -> NavigationHandler.Navigation.TEXTCINEMATIC
            else -> NavigationHandler.Navigation.GAME
        }
    }

    /**
     * @return boolean
     */
    private fun requiresEnd(): Boolean {
        val array = ArrayList<String>().apply{
            add("9a")
            add("9b")
            add("9d")
            add("10a")
        }
        return array.contains(symbols.chapterCode)
    }


}
