package com.purpletear.game.presentation.common

import android.content.Context
import android.provider.Settings
import kotlin.math.roundToInt

/** Coil's drawable fades do not observe Compose's MotionDurationScale. */
internal fun Context.storyImageCrossfadeMillis(): Int {
    val scale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        .takeIf { it.isFinite() } ?: 1f
    return (180 * scale.coerceIn(0f, 10f)).roundToInt()
}
