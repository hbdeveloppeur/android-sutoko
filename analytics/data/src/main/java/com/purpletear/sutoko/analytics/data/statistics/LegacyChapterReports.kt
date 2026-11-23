package com.purpletear.sutoko.analytics.data.statistics

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.core.domain.analytics.ChapterStatistics
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChapterStatisticsEntryPoint { fun chapterStatistics(): ChapterStatistics }

/** Explicit bridge for legacy Java readers, which are not Hilt activities. */
object LegacyChapterReports {
    @JvmStatic fun entered(context: Context,gameId: Int,language: String,chapter: String) {
        runCatching { reporter(context).entered("friendzoned",gameId.toString(),language,chapter) }
    }
    @JvmStatic fun completed(context: Context,gameId: Int,language: String,chapter: String) {
        runCatching { reporter(context).completed("friendzoned",gameId.toString(),language,chapter) }
    }
    @JvmStatic fun storyFinished(context: Context,gameId: Int,language: String,chapter: String) {
        runCatching { reporter(context).storyFinished("friendzoned",gameId.toString(),language,chapter) }
    }
    @JvmStatic fun observe(activity: Activity,gameId: Int,language: String,chapter: String) {
        runCatching {
            val reports=reporter(activity)
            (activity as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) { reports.active("friendzoned",gameId.toString(),language,chapter) }
            })
        }
    }
    private fun reporter(context: Context): ChapterStatistics = EntryPointAccessors.fromApplication(context.applicationContext,ChapterStatisticsEntryPoint::class.java).chapterStatistics()
}
