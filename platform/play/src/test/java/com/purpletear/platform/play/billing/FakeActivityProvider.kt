package com.purpletear.platform.play.billing

import android.app.Activity
import com.purpletear.sutoko.core.android.di.ActivityProvider

internal class FakeActivityProvider : ActivityProvider {
    var currentActivity: Activity? = null
    override fun getActivity(): Activity? = currentActivity
}
