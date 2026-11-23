package com.purpletear.ads

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ChapterBreakGate(
    private val activity: Activity,
    savedState: Bundle?,
    private val legacyId: Int,
    private val bypassAds: Boolean,
    private val navigate: Runnable,
) : DefaultLifecycleObserver {
    private val navigation = ChapterBreakNavigation(
        savedState?.getBoolean("ads_child_launched") == true && savedState.getBoolean("ads_busy") != true,
        savedState?.getString("ads_consumed_token"),
    )
    private var resumed = false
    private var pendingContinuation = false

    init { (activity as LifecycleOwner).lifecycle.addObserver(this) }

    override fun onResume(owner: LifecycleOwner) {
        resumed = true
        advance()
    }
    override fun onPause(owner: LifecycleOwner) { resumed = false }

    fun childReturned(data: Intent?, destinationChapter: Int) {
        navigation.childReturned(data?.getStringExtra(ReaderAds.CHAPTER_TRANSITION),
            !bypassAds && candidateChapter(legacyId, destinationChapter))
        if (resumed) activity.window.decorView.post { advance() }
    }

    fun save(outState: Bundle) {
        outState.putBoolean("ads_child_launched", navigation.childLaunched)
        outState.putBoolean("ads_busy", navigation.busy)
        outState.putString("ads_consumed_token", navigation.consumedToken)
    }

    private fun advance() {
        if (!resumed || activity.isFinishing || activity.isDestroyed || navigation.busy) return
        if (pendingContinuation) {
            pendingContinuation = false
            launch()
            return
        }
        if (navigation.candidate == null) { launch(); return }
        if (!navigation.begin()) return
        ReaderAds.coordinator(activity).atChapterBreak(activity, ReaderAds.legacyKey(legacyId), "friendzoned", {}, {
            navigation.finish()
            pendingContinuation = true
            if (resumed) advance()
        })
    }

    private fun launch() { if (navigation.launch()) navigate.run() }

    companion object {
        @JvmStatic fun candidateChapter(legacyId: Int, chapter: Int): Boolean = when (legacyId) {
            162 -> chapter in setOf(5, 6, 8, 9)
            161 -> chapter in setOf(2, 4, 6, 8, 9)
            159 -> chapter in setOf(2, 4, 6, 9)
            163 -> chapter in setOf(4, 6, 9)
            else -> false
        }
    }
}
