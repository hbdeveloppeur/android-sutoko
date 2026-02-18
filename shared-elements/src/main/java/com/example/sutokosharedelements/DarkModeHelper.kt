package com.example.sharedelements

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import androidx.annotation.Keep

@Keep
object DarkModeHelper {
    private const val key: String = "SUTOKO_DARK_MODE"


    fun isDarkMode(activity: Activity): Boolean {
        val powerManager: PowerManager =
            activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        val s: SharedPreferences = activity.getSharedPreferences(
            key,
            Context.MODE_PRIVATE
        )
        val exists = s.contains("enabled")
        if (!exists && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode) {
            return true
        }

        return s.getBoolean("enabled", false)
    }

    @SuppressLint("CommitPrefEdits")
    fun setDarkMode(activity: Activity, value: Boolean) {
        val s: SharedPreferences = activity.getSharedPreferences(
            key,
            Context.MODE_PRIVATE
        )
        s.edit().putBoolean("enabled", value).apply()
    }
}