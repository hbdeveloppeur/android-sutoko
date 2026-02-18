package fr.purpletear.sutoko.screens.web

import android.view.View
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.GraphicsPreference
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.tools.Animation

class WebActivityGraphics {

    companion object {

        enum class Views(val id: Int) {
            BUYING_VIEW_ROOT(R.id.sutoko_web_buying_root),
            BUYING_ICON(R.id.sutoko_buying_product_image),
            BUYING_TEXT(R.id.sutoko_buying_product_text),
            BUYING_LOADER(R.id.sutoko_buying_product_loader)
        }

        /**
         * Fades the filter
         *
         * @param activity
         * @param isVisible
         */
        fun fadeLoadingFilter(activity: WebActivity, isVisible: Boolean) {
            val visibility =
                if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT
            Animation.setAnimation(
                activity.findViewById(R.id.sutoko_web_filter),
                visibility,
                activity,
                280
            )

            Animation.setAnimation(
                activity.findViewById(R.id.sutoko_web_progressbar),
                visibility,
                activity,
                280
            )

            Animation.setAnimation(
                activity.findViewById(R.id.sutoko_web_progressbar_text),
                visibility,
                activity,
                280
            )
        }

        fun setImage(activity: WebActivity, requestManager: RequestManager) {
            requestManager.load(R.drawable.ic_checked_pinky).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.CACHE
                )
            ).into(activity.findViewById(Views.BUYING_ICON.id))
        }

        fun setBuyingText(activity: WebActivity, str: String) {
            activity.findViewById<TextView>(Views.BUYING_TEXT.id).text = str
        }

        fun setBuyingViewVisibility(activity: WebActivity, isVisible: Boolean) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            activity.findViewById<View>(Views.BUYING_VIEW_ROOT.id).visibility = visibility
        }

        fun setBuyingViewLoaderVisibility(activity: WebActivity, isVisible: Boolean) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            activity.findViewById<View>(Views.BUYING_LOADER.id).visibility = visibility
        }

        fun setBuyingViewIconVisibility(activity: WebActivity, isVisible: Boolean) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            activity.findViewById<View>(Views.BUYING_ICON.id).visibility = visibility
        }
    }
}