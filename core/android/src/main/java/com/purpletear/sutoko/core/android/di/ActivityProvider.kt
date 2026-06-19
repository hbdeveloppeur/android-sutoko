package com.purpletear.sutoko.core.android.di

import android.app.Activity

fun interface ActivityProvider {
    fun getActivity(): Activity?
}
