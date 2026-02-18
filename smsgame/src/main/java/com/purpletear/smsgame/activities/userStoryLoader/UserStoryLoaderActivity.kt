package com.purpletear.smsgame.activities.userStoryLoader

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.sharedelements.Data
import com.purpletear.smsgame.activities.smsgame.objects.Story

class UserStoryLoaderActivity : AppCompatActivity() {
    private lateinit var model: UserStoryLoaderModel
    private lateinit var graphics: UserStoryLoaderGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(UserStoryReferences.layoutId)
        load()

        // Receives and maintain url story
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
        model.handleSharedStoryUrl(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) {
            return
        }
        model.handleSharedStoryUrl(intent)
    }

    override fun onStop() {
        graphics.delayHandler.stop()
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics.updateFilterVisibility(this, false) {
                model.loadStory(this, ::onGameLoaded, ::onError)
            }
        }
    }

    private fun onGameLoaded(story: Story) {
        UserStoryLoaderGraphics.setAuthorName(this, story.authorCachedName)
        UserStoryLoaderGraphics.setTitle(this, story.title)

        UserStoryLoaderGraphics.setAuthorProfilPicture(this, model.requestManager, story)
        graphics.startAnimation(this) {
            graphics.updateFilterVisibility(this, true, ::onFinish)
        }
    }

    private fun onFinish() {
        model.startActivity(this)
    }

    private fun onError(error: SutokoError) {
        Log.e("UserStoryLoaderActivity", "onError: $error")
    }

    private fun load() {
        model = UserStoryLoaderModel(this)
        graphics = UserStoryLoaderGraphics()
    }

    companion object {

        fun require(activity: Activity, story: Story): Intent {
            val intent = Intent(activity, UserStoryLoaderActivity::class.java)
            intent.putExtra(Data.Companion.Extra.STORY.id, story)
            return intent
        }
    }
}