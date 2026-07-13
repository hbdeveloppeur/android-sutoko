package com.example.sharedelements.utils

import android.app.Activity
import android.os.Build

/**
 * Compatibility helper for Activity open transitions.
 *
 * [Activity.overridePendingTransition] is deprecated from API 34. This helper uses the
 * recommended [Activity.overrideActivityTransition] on API 34+ and falls back to the
 * deprecated API on older versions.
 */
object ActivityTransitionHelper {

    @JvmStatic
    fun overrideOpenTransition(activity: Activity, enterAnim: Int, exitAnim: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                enterAnim,
                exitAnim,
            )
        } else {
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(enterAnim, exitAnim)
        }
    }
}
