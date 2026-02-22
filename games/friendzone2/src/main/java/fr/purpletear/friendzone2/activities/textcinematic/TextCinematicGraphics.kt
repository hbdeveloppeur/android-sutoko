package fr.purpletear.friendzone2.activities.textcinematic

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import fr.purpletear.friendzone2.R
import android.animation.ValueAnimator
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.tables.Character
import purpletear.fr.purpleteartools.*


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
     * Fades the image out
     * @param a : Activity
     * @param onCompletion : () -> Unit
     */
    fun fadeOutImage(a: Activity, onCompletion: () -> Unit) {
        val image = a.findViewById<View>(R.id.textcinematic_background)
        Animation.setAnimation(image, Animation.Animations.ANIMATION_FADEOUT, a, fadeInDuration)

        val runnable = object : Runnable2("Demande de changement d'image de fond ", fadeInDuration) {
            override fun run() {
                onCompletion()
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
        Std.debug("Fading text ${ if(isVisible){"in"}else{"out"} }...")
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


            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "bg_stats_bar")).into(a.findViewById(R.id.inc_stats_background))
            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "bg_stats")).into(a.findViewById(R.id.stats_background))
            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "fz3")).into(a.findViewById(R.id.friendzone2_end_logo))
            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_instagram")).into(a.findViewById(R.id.end_icon_instagram))
            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "rate")).into(a.findViewById(R.id.end_rate))
            rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "evasaved")).into(a.findViewById(R.id.end_background))
        }

        /**
         * Fades in the rate screen
         * @param a : Activity
         */
        fun fadeRateScreen(a : Activity) {
            val screen = a.findViewById<View>(R.id.textcinematic_end)
            Animation.setAnimation(
                    screen,
                    Animation.Animations.ANIMATION_FADEIN,
                    a
            )
        }

        /**
         * Fades in the stats screen
         * @param a : Activity
         * @param isVisible : Boolean
         */
        fun fadeStatsScreen(a : Activity, isVisible: Boolean) {
            val screen = a.findViewById<View>(R.id.textcinematic_stats)
            Animation.setAnimation(
                    screen,
                    if(isVisible) {Animation.Animations.ANIMATION_FADEIN} else {Animation.Animations.ANIMATION_FADEOUT},
                    a
            )
        }

        /**
         * Fades the filter out
         * @param a : Activity
         */
        fun fadeFilter(a : Activity) {
            val filter = a.findViewById<View>(R.id.textcinematic_filter)
            Animation.setAnimation(
                    filter,
                    Animation.Animations.ANIMATION_FADEOUT,
                    a,
                    1280
            )
        }

        /**
         * Sets the stats' text
         * These stats are the average of one year of french players
         * @param a : Activity
         */
        fun setStatsText(a: Activity) {
            setStatsText(a, 1)
            setStatsText(a, 2)
            setStatsText(a, 3)
            setStatsText(a, 4)
        }

        /**
         * Sets the stats' text
         * @param a : Activity
         * @param number : Int
         */
        fun setStatsText(a: Activity, number : Int) {
            val viewId = when(number) {
                1 -> R.id.stats_layout1
                2 -> R.id.stats_layout2
                3 -> R.id.stats_layout3
                4 -> R.id.stats_layout4
                else -> throw IllegalArgumentException()
            }
            val sentence = when(number) {
                1 -> a.getString(R.string.stats_p1, getPercent(number))
                2 -> a.getString(R.string.stats_p2, getPercent(number))
                3 -> a.getString(R.string.stats_p3, getPercent(number))
                4 -> a.getString(R.string.stats_p4, getPercent(number))
                else -> throw IllegalArgumentException()
            }
            val layout = a.findViewById<View>(viewId)
            layout.findViewById<TextView>(R.id.stats_text_percent).text = a.getString(R.string.percent, getPercent(number))
            layout.findViewById<TextView>(R.id.inc_stat_text).text = sentence
        }


        /**
         * Animates the stats bar
         * @param a : Activity
         * @param number : Int
         */
        fun animateStatsBar(a : Activity, number : Int) {
            val viewId = when(number) {
                1 -> R.id.stats_layout1
                2 -> R.id.stats_layout2
                3 -> R.id.stats_layout3
                4 -> R.id.stats_layout4
                else -> throw IllegalArgumentException()
            }
            animateStatsBar(a.findViewById<View>(viewId), getPercent(number))
        }

        private fun getPercent(number : Int): Int {
            return when(number) {
                1 -> 67
                2 -> 15
                3 -> 10
                4 -> 8
                else -> throw IllegalArgumentException()
            }
        }

        /**
         * Animates the stats bar
         * @param statsLayout : View
         * @param percent : Int
         */
        private fun animateStatsBar(statsLayout : View, percent : Int) {
            val baseWidth = statsLayout.findViewById<View>(R.id.inc_stats_background).width
            val endWidth = baseWidth * percent / 100
            val bar = statsLayout.findViewById<View>(R.id.inc_stats_bar)
            val va = ValueAnimator.ofInt(0, endWidth)
            va.duration = 1280
            va.addUpdateListener {
                animation ->
                val layoutparams = bar.layoutParams
                layoutparams.width = animation.animatedValue as Int
                bar.layoutParams = layoutparams
            }
            va.repeatCount = 0
            va.start()
        }
    }
}