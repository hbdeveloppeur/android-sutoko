package fr.purpletear.friendzone2.activities.phone.photos

import android.content.Context
import com.bumptech.glide.RequestManager

class PhotosScreenModel(context : Context, requestManager: RequestManager) {
    var adapter = Adapter(context, requestManager)
    private set
    var requestManager: RequestManager = requestManager
        private set

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true
    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }
}
