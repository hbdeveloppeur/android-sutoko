package com.purpletear.ads

import android.app.Activity
import android.content.Intent
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.UUID

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReaderAdsEntryPoint { fun chapterInterstitials(): ChapterInterstitials }

object ReaderAds {
    const val CHAPTER_TRANSITION = "reader_ads_chapter_transition"

    @JvmStatic fun coordinator(activity: Activity): ChapterInterstitials =
        EntryPointAccessors.fromApplication(activity.applicationContext, ReaderAdsEntryPoint::class.java)
            .chapterInterstitials()

    @JvmStatic fun track(activity: Activity, gameKey: String) {
        val ads = coordinator(activity)
        val id = System.identityHashCode(activity)
        ads.prepare(gameKey)
        (activity as LifecycleOwner).lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) { ads.readerResumed(id) }
            override fun onPause(owner: LifecycleOwner) { ads.readerPaused(id) }
            override fun onDestroy(owner: LifecycleOwner) { ads.readerPaused(id) }
        })
    }

    @JvmStatic fun onInput(activity: Activity, event: InputEvent) {
        if (isDeliberateReaderInput(event)) coordinator(activity).userAction(System.identityHashCode(activity))
    }

    @JvmStatic fun chapterResult(intent: Intent): Intent =
        intent.putExtra(CHAPTER_TRANSITION, UUID.randomUUID().toString())

    @JvmStatic fun legacyKey(legacyId: Int): String = "legacy:$legacyId"
}

internal fun isDeliberateReaderInput(event: InputEvent): Boolean = when (event) {
    is MotionEvent -> event.actionMasked == MotionEvent.ACTION_DOWN
    is KeyEvent -> event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 &&
        (event.keyCode == KeyEvent.KEYCODE_BACK || !event.isSystem)
    else -> false
}
