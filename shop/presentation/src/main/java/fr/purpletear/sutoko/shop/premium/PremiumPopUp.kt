package fr.purpletear.sutoko.shop.premium

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.example.sutokosharedelements.GraphicsPreference
import fr.purpletear.sutoko.shop.presentation.R
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.FingerV2

object PremiumPopUp {
    val layoutId: Int = R.layout.sutoko_popup_passpremium_cta
    const val VIEW_TAG: String = "premium_pop_up"

    private enum class Views(val id: Int) {
        HEADER_IMAGE(R.id.sutoko_popup_header_image),
        HEADER_IMAGE_PARENT(R.id.sutoko_popup_night),
        FOREGROUND_IMAGE(R.id.sutoko_popup_foreground_image),
        BUTTON(R.id.sutoko_popup_button),
        DESCRIPTION(R.id.sutoko_popup_description),
        CARD(R.id.sutoko_premium_popup_card),
        BACKGROUND_HITBOX(R.id.sutoko_popup_background)
    }

    /**
     * Inserts the popup in the window
     * @param activity Activity?
     * @param requestManager RequestManager
     * @param parent ViewGroup
     * @param onAttached Function0<Unit>
     */
    fun insertPopUp(
        activity: Activity?,
        requestManager: RequestManager,
        parent: ViewGroup,
        onAttached: () -> Unit
    ) {
        if (activity == null) {
            return
        }
        val view = activity.layoutInflater.inflate(layoutId, parent, false)
        view.visibility = View.INVISIBLE
        view.tag = VIEW_TAG

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            @Suppress("SENSELESS_COMPARISON")
            override fun onViewAttachedToWindow(v: View) {
                val ready = activity != null && !activity.isFinishing
                if (ready) {
                    setListeners(activity, activity as PremiumPopUpListener)
                    design(activity, requestManager)
                    Handler(Looper.getMainLooper()).post(onAttached)
                }
            }

            override fun onViewDetachedFromWindow(v: View) {

            }
        })

        parent.addView(view)
    }

    fun design(activity: Activity?, requestManager: RequestManager) {
        if (activity != null && !activity.isFinishing) {

            requestManager.load(R.drawable.account_creation_character).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(activity.findViewById(Views.FOREGROUND_IMAGE.id))

            requestManager.load(R.drawable.sutoko_premium_night).apply(
                GraphicsPreference.getRequestOptions(
                    GraphicsPreference.Level.DONT_CACHE
                )
            )
                .transition(withCrossFade())
                .into(activity.findViewById(Views.HEADER_IMAGE.id))

            loadButtonText(activity) { text ->
                setButtonText(activity, text)
            }
            loadPremiumDescription(activity) { text ->
                setDescriptionText(activity, text)
            }
        }
    }

    fun loadButtonText(activity: Activity, onLoaded: (String) -> Unit) {

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 6000
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        val map: MutableMap<String, Any> = mutableMapOf(
            Pair(
                "premium_cta_button_text",
                activity.getString(R.string.sutoko_try_premium)
            )
        )
        remoteConfig.setDefaultsAsync(map)

        remoteConfig.fetchAndActivate().addOnCompleteListener foo@{
            if (activity.isFinishing) {
                return@foo
            }
            onLoaded(remoteConfig.getString("premium_cta_button_text"))
        }
    }

    private fun loadPremiumDescription(activity: Activity, onLoaded: (String) -> Unit) {

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        val map: MutableMap<String, Any> = mutableMapOf(
            Pair(
                "premium_cta_desc_text",
                activity.getString(R.string.sutoko_premium_description_cta)
            )
        )
        remoteConfig.setDefaultsAsync(map)

        remoteConfig.fetchAndActivate().addOnCompleteListener foo@{
            if (activity.isFinishing) {
                return@foo
            }
            onLoaded(remoteConfig.getString("premium_cta_desc_text"))
        }
    }

    private fun setButtonText(activity: Activity, text: String) {
        activity.findViewById<TextView>(Views.BUTTON.id).text = text
    }

    private fun setDescriptionText(activity: Activity, text: String) {
        activity.findViewById<TextView>(Views.DESCRIPTION.id).text = text
    }


    fun animate(activity: Activity, viewRoot: View, isVisible: Boolean) {
        val view = viewRoot.findViewWithTag<View>(VIEW_TAG) ?: return
        if (isVisible) {
            view.visibility = View.VISIBLE
            Animation.setAnimation(
                view.findViewById(R.id.sutoko_popup_night),
                Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM,
                activity
            )
            Animation.setAnimation(
                view.findViewById(R.id.sutoko_premium_popup_card),
                Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM,
                activity
            )
            Animation.setAnimation(
                view.findViewById(R.id.sutoko_popup_foreground_image),
                Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM,
                activity
            )
        } else {
            hideElements(activity)
            view.visibility = View.GONE
        }
    }

    fun hideElements(activity: Activity) {
        activity.findViewById<View>(Views.FOREGROUND_IMAGE.id)?.visibility = View.INVISIBLE
        activity.findViewById<View>(Views.HEADER_IMAGE_PARENT.id)?.visibility = View.INVISIBLE
        activity.findViewById<View>(Views.CARD.id)?.visibility = View.INVISIBLE
    }

    fun setListeners(activity: Activity, callback: PremiumPopUpListener) {
        FingerV2.register(activity, Views.BUTTON.id, callback::onButtonPressed)
        FingerV2.register(activity, Views.BACKGROUND_HITBOX.id, callback::onUserCancel)
    }
}