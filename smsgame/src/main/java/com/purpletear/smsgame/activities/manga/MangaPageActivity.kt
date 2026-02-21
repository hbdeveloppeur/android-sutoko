package com.purpletear.smsgame.activities.manga

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.sutokosharedelements.Data
import com.example.sharedelements.SmsGameTreeStructure
import com.purpletear.smsgame.BuildConfig
import com.purpletear.smsgame.databinding.ActivityMangaPageBinding

class MangaPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMangaPageBinding

    private val filename: String?
        get() = intent.getStringExtra(Data.Companion.Extra.MANGA_FILE_NAME.id)
    private val storyId: Int
        get() = intent.getIntExtra(Data.Companion.Extra.STORY_ID.id, -1)
    private val mangaMessages: ArrayList<MangaMessage>
        get() = intent.getParcelableArrayListExtra(Data.Companion.Extra.MANGA_MESSAGES.id)
            ?: ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMangaPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setImage()
    }


    private fun setImage() {
        if (filename.isNullOrBlank() || storyId == -1) {
            if (BuildConfig.DEBUG) {
                throw IllegalStateException("Invalid values")
            }
            setResult(RESULT_OK)
            finish()
            return
        }

        Glide.with(this)
            .asBitmap()
            .load(SmsGameTreeStructure.getMediaFilePath(this, storyId, filename!!))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    var bitmap = resource
                    mangaMessages.forEach { message ->
                        bitmap = com.purpletear.smsgame.activities.manga.MangaHelper.drawText(
                            this@MangaPageActivity,
                            resource,
                            message.text,
                            message.size,
                            message.x,
                            message.y,
                            message.w
                        )
                    }
                    binding.sutokoMangapagePage.setImageBitmap(bitmap)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Clear
                }
            })
    }

    companion object {

        fun require(
            activity: Activity,
            filename: String,
            storyId: Int,
            mangaMessages: ArrayList<MangaMessage>
        ): Intent {
            val intent = Intent(activity, MangaPageActivity::class.java)
            intent.putExtra(Data.Companion.Extra.MANGA_FILE_NAME.id, filename)
            intent.putExtra(Data.Companion.Extra.STORY_ID.id, storyId)
            intent.putParcelableArrayListExtra(
                Data.Companion.Extra.MANGA_MESSAGES.id,
                mangaMessages
            )
            return intent
        }
    }
}