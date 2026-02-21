package fr.purpletear.sutoko.shop.premium

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sutokosharedelements.GraphicsPreference
import fr.purpletear.sutoko.shop.presentation.R
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.FingerV2


class PremiumSucessGraphics {
    private var isAttached: Boolean = false
    var layout: View? = null
        private set

    companion object {
        private enum class Page(val layoutId: Int) {
            SUSCRIBE_SUCCESS(R.layout.page_premium_success)
        }

        private enum class Views(val id: Int, val animate: Boolean) {
            CONTENT(R.id.sutoko_page_premium_success_content, true),
            CANCEL_HITBOX(R.id.sutoko_page_premium_success_cancel_hitbox, false),
            ANIMATION(R.id.sutoko_page_premium_success_animation, true),
            HEADER_CARDVIEW(R.id.sutoko_page_premium_success_cardview, true),
            HEADER_CARDVIEW_IMAGEVIEW(R.id.sutoko_page_premium_success_cardview_image, false),
            HEADER_CHARACTERS(R.id.sutoko_page_premium_success_characters, true),
            CHECKED1(R.id.sutoko_premium_success_checked_1, false),
            CHECKED2(R.id.sutoko_premium_success_checked_2, false),
            CHECKED3(R.id.sutoko_premium_success_checked_3, false)
        }

        fun launch(
            activity: Activity,
            requestManager: RequestManager,
            onButtonPressed: (() -> Unit)? = null
        ) {
            val premiumSuccessPage = PremiumSucessGraphics()
            premiumSuccessPage.attach(activity, object : PremiumSuccesPageListener {
                override fun onViewAttached() {
                    if (activity.isFinishing) {
                        return
                    }
                    design(premiumSuccessPage.layout ?: return, requestManager)
                    premiumSuccessPage.show(activity)
                }

                override fun onContinueButtonPressed() {
                    if (activity.isFinishing) {
                        return
                    }
                    premiumSuccessPage.hide()
                    if (onButtonPressed != null) {
                        Handler(Looper.getMainLooper()).post(onButtonPressed)
                    }
                }

                override fun onUserCancel() {
                    if (activity.isFinishing) {
                        return
                    }
                    premiumSuccessPage.hide()
                }
            })
        }

        fun design(itemView: View, requestManager: RequestManager) {
            requestManager.load(R.drawable.account_creation_character).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.HEADER_CHARACTERS.id))
            requestManager.load(R.drawable.sutoko_premium_night).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.HEADER_CARDVIEW_IMAGEVIEW.id))
            requestManager.load(R.drawable.ic_checked_optimized).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.CHECKED1.id))
            requestManager.load(R.drawable.ic_checked_optimized).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.CHECKED2.id))
            requestManager.load(R.drawable.ic_checked_optimized).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(itemView.findViewById(Views.CHECKED3.id))
        }
    }

    fun attach(activity: Activity, listener: PremiumSuccesPageListener) {
        if (isAttached) {
            Handler(Looper.getMainLooper()).post(listener::onViewAttached)
            return
        }
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            .getChildAt(0) as ViewGroup
        layout =
            LayoutInflater.from(activity).inflate(Page.SUSCRIBE_SUCCESS.layoutId, parent, false)
        layout!!.visibility = View.INVISIBLE

        FingerV2.register(
            layout as View,
            R.id.sutoko_page_premium_success_button_continue,
            listener::onContinueButtonPressed
        )
        FingerV2.register(layout as View, Views.CANCEL_HITBOX.id, listener::onUserCancel)

        layout!!.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) {
                isAttached = true
                Handler(Looper.getMainLooper()).post(listener::onViewAttached)
            }

            override fun onViewDetachedFromWindow(v: View) {
                isAttached = false
            }
        })
        parent.addView(layout)
    }


    fun show(activity: Activity) {
        layout?.visibility = View.VISIBLE
        Views.values().forEach { view ->
            if (view.animate) {
                Animation.setAnimation(
                    layout?.findViewById(view.id)!!,
                    Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM,
                    activity
                )
            }
        }
    }

    fun hide() {
        layout?.findViewById<View>(Views.CONTENT.id)?.visibility = View.INVISIBLE
        layout?.findViewById<View>(Views.ANIMATION.id)?.visibility = View.INVISIBLE
        layout?.findViewById<View>(Views.HEADER_CARDVIEW.id)?.visibility = View.INVISIBLE
        layout?.findViewById<View>(Views.HEADER_CHARACTERS.id)?.visibility = View.INVISIBLE
        layout?.visibility = View.GONE

    }

}

interface PremiumSuccesPageListener {
    fun onViewAttached()
    fun onContinueButtonPressed()
    fun onUserCancel()
}