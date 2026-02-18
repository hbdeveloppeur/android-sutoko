package fr.purpletear.sutoko.screens.directive

import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

class DirectivesActivityModel(
    activity: DirectivesActivity
) {

    val requestManager: RequestManager = Glide.with(activity)
    var isFirstStart: Boolean = true
        get() {
            val v = field;
            field = false;
            return v;
        }
}