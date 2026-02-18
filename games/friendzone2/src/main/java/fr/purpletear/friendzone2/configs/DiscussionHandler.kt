/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.configs

import android.util.Log
import fr.purpletear.friendzone2.BuildConfig

internal object DiscussionHandler {

    fun execute(name: String, `when`: Boolean): Boolean {
        if (BuildConfig.DEBUG && `when`) {
            Log.i("purpleteardebug", name)
        }
        return `when`
    }

    fun execute(name: String, runnable: Runnable) {
        if (BuildConfig.DEBUG) {
            Log.i("purpleteardebug", name)
        }
        runnable.run()
    }
}
