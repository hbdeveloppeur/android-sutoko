package com.purpletear.sutoko.core.android.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultActivityProvider @Inject constructor() : ActivityProvider,
    Application.ActivityLifecycleCallbacks {

    private var activityRef: WeakReference<Activity>? = null

    override fun getActivity(): Activity? = activityRef?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }
}
