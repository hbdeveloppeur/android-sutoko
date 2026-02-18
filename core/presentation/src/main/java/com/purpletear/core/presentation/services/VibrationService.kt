package com.purpletear.core.presentation.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService


/**
 * Performs a vibration on the device.
 */
fun performVibration(context: Context, duration: Long = 50L) {
    val vibrator: Vibrator? = context.getSystemService()

    vibrator?.let { vib ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vib.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.EFFECT_DOUBLE_CLICK
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(duration)
        }
    }
}