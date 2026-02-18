/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.main

import fr.purpletear.friendzone.config.Phrase

interface MainInterface {
    fun onClickChoice(p: Phrase)
    fun onInsertPhrase(position: Int, isSmoothScroll: Boolean)
}
