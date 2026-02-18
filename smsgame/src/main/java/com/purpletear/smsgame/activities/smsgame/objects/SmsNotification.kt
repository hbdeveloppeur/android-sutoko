package com.purpletear.smsgame.activities.smsgame.objects

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.purpletear.smsgame.R
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.FingerV2
import purpletear.fr.purpleteartools.SoundHandler

class SmsNotification(
    private val requestManager: RequestManager,
    private val title: String,
    private val description: String,
    private val actionText: String,
    private val image: Any
) {
    var isAttached: Boolean = false
        private set
    private val delayHandler: DelayHandler = DelayHandler()
    private var layout: View? = null
    private var soundHandler: SoundHandler = SoundHandler("sound/intuition.mp3")


    /**
     * Inserts the sms activity
     *
     * @param activity
     */
    fun attach(activity: Activity, onViewAttached: (() -> Unit)?, onTouched: (() -> Unit)?) {
        if (isAttached) {
            return
        }
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            .getChildAt(0) as ViewGroup
        layout = LayoutInflater.from(activity).inflate(R.layout.inc_sms_notification, parent, false)
        layout!!.visibility = View.VISIBLE

        setContent(layout!!, requestManager, title, description, actionText, image)

        FingerV2.register(layout as View, R.id.smsnotification_item, onTouched!!)

        layout!!.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) {
                isAttached = true
                if (onViewAttached != null) {
                    Handler(Looper.getMainLooper()).post(onViewAttached)
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                isAttached = false
            }
        })
        parent.addView(layout)
    }

    /**
     * Detaches the view
     *
     * @param activity
     */
    fun detach(activity: Activity) {
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            .getChildAt(0) as ViewGroup
        parent.removeView(layout!!)
        isAttached = false
    }

    /**
     * Animates the notification
     *
     * @param activity
     * @param appears
     * @param delay
     */
    fun animate(activity: Activity, appears: Boolean, delay: Int = 0, onCompletion: (() -> Unit)?) {
        if (appears) {
            delayHandler.operation("animate", delay) {
                appearsView(activity, layout!!)
                soundHandler.generate("sound/intuition.mp3", activity, false)
                soundHandler.play("sound/intuition.mp3")
                val duration = appearsAnimation(activity)
                if (null != onCompletion) {
                    delayHandler.operation("animate", duration.toInt(), onCompletion)
                }
            }
        } else {
            delayHandler.operation("animate", delay) {
                disappearsAnimation(activity)

                delayHandler.operation("animate", 280) {
                    val duration = disappearsView(activity, layout!!)
                    if (null != onCompletion) {
                        delayHandler.operation("animate", duration.toInt(), onCompletion)
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Sets the content of the notification
         * @param root : View
         * @param requestManager : RequestManager
         * @param title : String
         * @param description : String
         * @param actionText : String
         * @param image : Any
         */
        private fun setContent(
            root: View,
            requestManager: RequestManager,
            title: String,
            description: String,
            actionText: String,
            image: Any
        ) {
            root.findViewById<TextView>(R.id.notification_phrase_first).text = title
            root.findViewById<TextView>(R.id.notification_phrase_second).text = description
            root.findViewById<TextView>(R.id.notification_phrase_third).text = actionText
            if (image is String) {
                requestManager.load(image).into(root.findViewById(R.id.notification_preview_avatar))
            } else if (image is Int) {

                requestManager.load(image).into(root.findViewById(R.id.notification_preview_avatar))
            }
        }

        /**
         * Main View appears
         *
         * @param activity
         * @param root
         */
        private fun appearsView(activity: Activity, root: View) {
            Animation.setAnimation(
                root,
                Animation.Animations.ANIMATION_FADEIN,
                activity
            )
        }

        /**
         * Main View disappears
         *
         * @param activity
         * @param root
         */
        private fun disappearsView(activity: Activity, root: View): Long {
            return Animation.setAnimation(
                root,
                Animation.Animations.ANIMATION_FADEOUT,
                activity
            )
        }

        /**
         * Animates the notification
         *
         * @param activity
         */
        private fun appearsAnimation(activity: Activity): Long {
            return Animation.setAnimation(
                activity.findViewById<View>(R.id.smsnotification_item),
                Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM,
                activity,
                280
            )
        }

        /**
         * Animates the notification
         *
         * @param activity
         */
        private fun disappearsAnimation(activity: Activity): Long {
            return Animation.setAnimation(
                activity.findViewById<View>(R.id.smsnotification_item),
                Animation.Animations.ANIMATION_SLIDE_OUT_TO_RIGHT,
                activity,
                360
            )
        }
    }
}