package friendzone3.purpletear.fr.friendzon3.textcinematic

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import friendzone3.purpletear.fr.friendzon3.R
import friendzone3.purpletear.fr.friendzon3.custom.Character
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.MemoryHandler
import purpletear.fr.purpleteartools.Runnable2
import purpletear.fr.purpleteartools.Std

class TextCinematicGraphics {
    // Manages handlers
    var mh = MemoryHandler()
    private var isFirstImage : Boolean = true
    private var isFirstText : Boolean  = true

    /**
     * Sets the image
     * @param a : Activity
     * @param rm : RequestManager
     * @param onCompletion : () -> Unit
     */
    fun setImage(a : Activity, rm : RequestManager, id : String, onCompletion : () -> Unit) {
        val image = a.findViewById<ImageView>(R.id.textcinematic_background)
        val text = a.findViewById<TextView>(R.id.textcinematic_text)

        if(isFirstImage) {
            isFirstImage = false
            rm.load(id).into(image)
            fadeInImage(a, onCompletion)
            return
        }
        isFirstText = true

        Animation.setAnimation(image, Animation.Animations.ANIMATION_FADEOUT, a, fadeInDuration)
        Animation.setAnimation(text, Animation.Animations.ANIMATION_FADEOUT, a, fadeInDuration)

        val runnable = object : Runnable2("Demande de changement d'image de fond ", fadeInDuration) {
            override fun run() {
                text.text = ""
                rm.load(id).into(image)
                fadeInImage(a, onCompletion)
            }
        }

        mh.push(runnable)
        mh.run(runnable)
    }

    /**
     * Fades the image in
     * @param a : Activity
     * @param onCompletion : () -> Unit
     */
    private fun fadeInImage(a: Activity, onCompletion: () -> Unit) {
        val image = a.findViewById<ImageView>(R.id.textcinematic_background)
        Animation.setAnimation(image, Animation.Animations.ANIMATION_FADEIN, a, fadeInDuration)

        val runnable = object : Runnable2("Demande de changement d'image de fond ", fadeInDuration) {
            override fun run() {
                Std.debug("Image appareation done")
                onCompletion()
            }
        }

        mh.push(runnable)
        mh.run(runnable)
    }

    /**
     * Sets the text
     * @param a : Activity
     * @param str : String
     * @param onCompletion : () -> Unit
     */
    fun setText(a: Activity, str : String, onCompletion: () -> Unit) {
        val text = a.findViewById<TextView>(R.id.textcinematic_text)
        text.text = Character.updateNames(a, str)
        fadeText(a, true, fadeInDuration, onCompletion)
    }


    /**
     * Fade out the text
     * @param a : Activity
     * @param isVisible : Boolean
     */
    fun fadeText(a: Activity, isVisible: Boolean, duration : Int = fadeInDuration){
        fadeText(a, isVisible, duration){}
    }

    /**
     * Fade out the text
     * @param a : Activity
     * @param isVisible : Boolean
     * @param onCompletion : () -> Unit
     */
    fun fadeText(a: Activity, isVisible : Boolean, duration : Int = fadeInDuration, onCompletion: () -> Unit) {
        if(isFirstText) {
            isFirstText = false
            onCompletion()
            return
        }
        val text = a.findViewById<TextView>(R.id.textcinematic_text)

        Animation.setAnimation(
                text,
                if(isVisible) {Animation.Animations.ANIMATION_FADEIN} else {Animation.Animations.ANIMATION_FADEOUT},
                a, duration)

        val runnable = object : Runnable2("Demande de changement de texte", duration) {
            override fun run() {
                Std.debug("Fading text ${ if(isVisible){"in"}else{"out"} } done")
                onCompletion()
            }
        }

        mh.push(runnable)
        mh.run(runnable)
    }

    companion object {
        /**
         * Fade in duration of the visual and textual elements
         */
        var fadeInDuration = 1000
        private set

        /**
         * Sets images
         * @param a: Activity
         * @param rm: RequestManager
         * @see com.bumptech.glide.RequestManager
         */
        fun setImages(a: Activity, rm : RequestManager) {
            val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)

            val originalNoCachRequestOptions = RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontTransform()
                    .override(SIZE_ORIGINAL)

            rm.load(R.drawable.friendzone3_logo).apply(originalNoCachRequestOptions).into(a.findViewById(R.id.friendzone3_end_logo))
        }



        /**
         * Fades the filter out
         * @param a : Activity
         */
        fun fadeFilter(a : Activity, isVisible : Boolean) {
            val filter = a.findViewById<View>(R.id.textcinematic_filter)
            Animation.setAnimation(
                    filter,
                    if(isVisible) Animation.Animations.ANIMATION_FADEOUT else Animation.Animations.ANIMATION_FADEIN,
                    a,
                    1280
            )
        }
    }
}