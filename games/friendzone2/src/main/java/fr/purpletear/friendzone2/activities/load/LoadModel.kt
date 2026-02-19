/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.load

import com.example.sharedelements.SutokoSharedElementsData
import purpletear.fr.purpleteartools.TableOfSymbols


internal class LoadModel(
    var symbols: TableOfSymbols, var isGranted: Boolean
) {

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

    var soundPosition: Int = 0
    var hasSeenTextCinematic: Boolean = false
    var hasSeenPoetry: Boolean = false


    init {
        invalidate()
        navigationHandler = NavigationHandler()
    }

    fun invalidate() {
        hasSeenTextCinematic = false
        hasSeenPoetry = false
    }

    fun require(): NavigationHandler.Navigation {
        return when {
            requiresTextIntro() -> NavigationHandler.Navigation.TEXTCINEMATIC
            requiresPoetry() -> NavigationHandler.Navigation.POETRY
            navigationHandler.toMenu() -> NavigationHandler.Navigation.MENU
            else -> NavigationHandler.Navigation.GAME
        }
    }

    /**
     * @return boolean
     */
    private fun requiresPoetry(): Boolean {
        val array = arrayOf("6a", "6b", "7a", "9a")
        return array.contains(symbols.chapterCode) && !hasSeenPoetry && SutokoSharedElementsData.IS_POETRY_ENABLED
    }

    private fun requiresTextIntro(): Boolean {
        val array = arrayOf("1a", "5a", "5b", "9f", "10a", "10b", "10c")
        return SutokoSharedElementsData.IS_TEXTCINEMATIC_ENABLED && array.contains(symbols.chapterCode) && !hasSeenTextCinematic
    }
}
