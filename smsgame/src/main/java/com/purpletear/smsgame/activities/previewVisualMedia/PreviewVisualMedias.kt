package com.purpletear.smsgame.activities.previewVisualMedia

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener

import com.example.sutokosharedelements.Data
import com.purpletear.smsgame.databinding.ActivityPreviewVisualMediasBinding

class PreviewVisualMedias : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewVisualMediasBinding
    private lateinit var model: PreviewVisualMediasModel
    private lateinit var graphics: PreviewVisualMediasGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityPreviewVisualMediasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawableids =
            intent.getStringArrayListExtra(Data.Companion.Extra.ARRAY_DRAWABLE_IDS.id)!!
        val currentPosition = intent.getIntExtra(Data.Companion.Extra.CURRENT_POSITION.id, 0)

        model = PreviewVisualMediasModel(Glide.with(this), drawableids, currentPosition)
        graphics = PreviewVisualMediasGraphics()

        setListeners()

        // Check if we're running on Android 5.0 or higher
        supportPostponeEnterTransition()
        this.binding.mediapreviewImage.load(
            model.getCurrentDrawableId(),
            drawableids[currentPosition]
        ) {
            supportStartPostponedEnterTransition()
        }
    }

    fun ImageView.load(id: String, placeholderId: String, onLoadingFinished: () -> Unit = {}) {
        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinished()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinished()
                return false
            }
        }

        model.requestManager
            .load(id)
            .centerCrop().dontTransform().dontAnimate()
            .listener(listener)
            .into(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            updateVisual()
        }
    }

    private fun setListeners() {
    }


    private fun updateVisual() {
        PreviewVisualMediasGraphics.setPreviousButtonEnabled(this, model.hasPrevious())
        PreviewVisualMediasGraphics.setNextButtonEnabled(this, model.hasNext())
    }

    companion object {
        fun startActivity(
            activity: Activity,
            intent: Intent,
            imageView: ImageView,
            drawableIds: ArrayList<Int>,
            currentPosition: Int
        ) {
            intent.putIntegerArrayListExtra(
                Data.Companion.Extra.ARRAY_DRAWABLE_IDS.id,
                drawableIds
            )
            intent.putExtra(Data.Companion.Extra.CURRENT_POSITION.id, currentPosition)

            // Check if we're running on Android 5.0 or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        imageView,
                        ViewCompat.getTransitionName(imageView) ?: "dontyoubreakingmyheart"
                    )
                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
            }

        }

        fun startActivityWithDrawableString(
            activity: Activity,
            intent: Intent,
            imageView: ImageView,
            drawableIds: ArrayList<String>,
            currentPosition: Int
        ) {
            intent.putStringArrayListExtra(
                Data.Companion.Extra.ARRAY_DRAWABLE_IDS.id,
                drawableIds
            )
            intent.putExtra(Data.Companion.Extra.CURRENT_POSITION.id, currentPosition)

            // Check if we're running on Android 5.0 or higher
            val options: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    imageView,
                    ViewCompat.getTransitionName(imageView) ?: "dontyoubreakingmyheart"
                )
            activity.startActivity(intent, options.toBundle())

        }
    }
}
