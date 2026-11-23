package com.purpletear.sutoko.analytics.data.statistics
import android.content.Context
import com.purpletear.sutoko.analytics.data.BuildConfig
internal object StatisticsQa {
    private const val PREFS="statistics-qa"
    fun key(context: Context): String = if(BuildConfig.DEBUG) context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("key","") ?: "" else ""
    fun enabled(context: Context): Boolean = key(context).matches(Regex("[a-f0-9]{64}"))
    fun configure(context: Context,key: String) { if(BuildConfig.DEBUG && key.matches(Regex("[a-f0-9]{64}"))) context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("key",key).commit() }
}
