package com.purpletear.game.presentation.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable

/**
 * Retrieves a Parcelable extra from the Intent, handling API level differences.
 *
 * @param name The name of the extra
 * @return The Parcelable value, or null if not found
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name) as? T
    }
}
