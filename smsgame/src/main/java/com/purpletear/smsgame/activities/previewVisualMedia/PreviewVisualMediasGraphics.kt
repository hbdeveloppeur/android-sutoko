package com.purpletear.smsgame.activities.previewVisualMedia

import android.app.Activity
import android.view.View
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.purpletear.smsgame.R


class PreviewVisualMediasGraphics {

    companion object {

        /**
         * Sets the initial images
         * @param a : Activity
         * @param rm : RequestManager
         */
        fun setImage(a: Activity, rm: RequestManager, drawableId: String) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)


            rm.load(drawableId).apply(requestOptions).dontTransform().transition(withCrossFade())
                .into(
                    a.findViewById(
                        R.id.mediapreview_image
                    )
                )
        }

        /**
         * Sets the previous button enabled
         * @param activity Activity
         * @param isEnabled Boolean
         */
        fun setPreviousButtonEnabled(activity: Activity, isEnabled: Boolean) {
            activity.findViewById<View>(R.id.mediapreview_button_previous_image).alpha =
                if (isEnabled) {
                    1f
                } else {
                    0.3f
                }
        }

        /**
         * Sets the next button enabled
         * @param activity Activity
         * @param isEnabled Boolean
         */
        fun setNextButtonEnabled(activity: Activity, isEnabled: Boolean) {
            activity.findViewById<View>(R.id.mediapreview_button_next_image).alpha =
                if (isEnabled) {
                    1f
                } else {
                    0.3f
                }
        }
    }
}