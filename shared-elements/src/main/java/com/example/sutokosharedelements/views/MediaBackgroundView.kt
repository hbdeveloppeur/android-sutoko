package com.example.sutokosharedelements.views

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.Keep
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.sutokosharedelements.GraphicsPreference
import purpletear.fr.purpleteartools.Animation

@Keep
class MediaBackgroundView : FrameLayout, CVideoViewListener {

    private var imageView: ImageView
    private var videoView: CVideoView
    private var activity: Activity? = null

    constructor(context: Context) : super(
        context
    )

    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun free() {
        this.videoView.free()
    }

    // Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {

        videoView = CVideoView(context)
        videoView.visibility = View.INVISIBLE
        videoView.initialize(true, this)
        this.addView(videoView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        this.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setVideo(activity: Activity?, url: String, isLooping: Boolean = false) {

        val powerManager: PowerManager =
            activity?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val canPlayVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            !powerManager.isPowerSaveMode
        } else {
            false
        }
        if (!canPlayVideo) {
            return
        }
        this.activity = activity
        this.videoView.setVideo(activity, url, isLooping)
        this.videoView.visibility = View.VISIBLE
    }

    fun pauseVideo() {
        Animation.setAnimation(
            this.imageView,
            Animation.Animations.ANIMATION_FADEIN,
            this.activity ?: return,
            280
        )
        this.videoView.pause()
    }

    fun isPlaying(): Boolean {
        return this.videoView.isPlaying()
    }

    fun setImageVisible() {
        this.imageView.visibility = View.VISIBLE
    }

    fun playVideo() {
        this.videoView.play()
    }

    /**
     * Sets the image from url using Glide
     * @param requestManager RequestManager
     * @param url String
     * @param onCompletion Function0<Unit>?
     */
    fun setImage(
        requestManager: RequestManager,
        url: String,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
        onCompletion: ((Boolean) -> Unit)? = null
    ) {
        imageView.visibility = View.VISIBLE
        imageView.scaleType = scaleType
        requestManager
            .load(url)
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
            .transition(withCrossFade())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (onCompletion != null) {
                        Handler(Looper.getMainLooper()).post {
                            onCompletion(false)
                        }
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (onCompletion != null) {
                        Handler(Looper.getMainLooper()).post {
                            onCompletion(true)
                        }
                    }
                    return false
                }
            })
            .into(this.imageView)

    }

    fun setImage(
        requestManager: RequestManager,
        id: Int,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
        onCompletion: ((Boolean) -> Unit)? = null
    ) {
        imageView.visibility = View.VISIBLE
        imageView.scaleType = scaleType
        requestManager
            .load(id)
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
            .transition(withCrossFade())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (onCompletion != null) {
                        Handler(Looper.getMainLooper()).post {
                            onCompletion(false)
                        }
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (onCompletion != null) {
                        Handler(Looper.getMainLooper()).post {
                            onCompletion(true)
                        }
                    }
                    return false
                }
            })
            .into(this.imageView)

    }


    override fun onVideoStarted() {
        Animation.setAnimation(
            this.imageView,
            Animation.Animations.ANIMATION_FADEOUT,
            this.activity ?: return,
            280
        )
    }

    fun stopVideo() {
        Animation.setAnimation(
            this.imageView,
            Animation.Animations.ANIMATION_FADEIN,
            this.activity ?: return,
            280
        )
    }


    override fun onVideoDetached() {
        this.imageView.visibility = View.VISIBLE
    }

    override fun onRequestError() {
        this.imageView.visibility = View.VISIBLE
    }

    private val currentRatio: String? = null
    private fun ratioEquals(ratio: String): Boolean {
        return currentRatio != null && currentRatio == ratio
    }

    fun updateSize(fromRatio: String, calculatedSize: Point?): Point? {
        if (calculatedSize == null) {
            return null
        }
        val videoParams = videoView.layoutParams as FrameLayout.LayoutParams
        val imageParams = imageView.layoutParams as FrameLayout.LayoutParams
        val size = determineContentSizePx(
            fromRatio,
            calculatedSize
        )
        videoParams.width = size.x
        videoParams.height = size.y
        videoParams.gravity = Gravity.CENTER

        imageParams.width = size.x
        imageParams.height = size.y
        imageParams.gravity = Gravity.CENTER
        videoView.layoutParams = videoParams
        imageView.layoutParams = imageParams

        return size
    }


    fun shouldResize(withRatio: String): Boolean {
        return !this.ratioEquals(withRatio)
    }

    private fun parseRatio(ratio: String): IntArray {
        val values = ratio.split(":".toRegex()).toTypedArray()
        return intArrayOf(values[0].toInt(), values[1].toInt())
    }

    private fun determineContentSizePx(ratio: String, supposedContainerSize: Point): Point {
        val values = parseRatio(ratio)
        val divibeByZero =
            supposedContainerSize.y.toDouble() == 0.0 || supposedContainerSize.x.toDouble() == 0.0 || values[0]
                .toDouble() == 0.0 || values[1].toDouble() == 0.0
        if (divibeByZero) {
            return supposedContainerSize
        }
        if ((values[1].toDouble() / values[0].toDouble()) < supposedContainerSize.y.toDouble() / supposedContainerSize.x.toDouble()) {
            // extend height
            return Point(
                supposedContainerSize.y * values[0] / values[1],
                supposedContainerSize.y
            )
        } else if (values[1].toDouble() / values[0].toDouble() > supposedContainerSize.y.toDouble() / supposedContainerSize.x.toDouble()) {
            // extend width
            return Point(
                supposedContainerSize.x,
                supposedContainerSize.x * values[1] / values[0]
            )
        }
        return supposedContainerSize
    }
}