package com.purpletear.smsgame.activities.userStoryLoader

import com.purpletear.smsgame.R


object UserStoryReferences {
    val layoutId: Int = R.layout.activity_user_story_loader

    enum class Views(val id: Int) {
        TEXT_CONTEXT(R.id.sutoko_userstoryloader_content_text_context),
        TEXT_TITLE(R.id.sutoko_userstoryloader_content_text_title),
        CREDITS(R.id.sutoko_userstoryloader_content_credits),
        CREDITS_TEXT(R.id.sutoko_userstoryloader_content_credits_text),
        CREDITS_IMAGE(R.id.sutoko_userstoryloader_content_credits_image),
        BACKGROUND_IMAGE(R.id.sutoko_userstoryloader_background_image),
        FILTER(R.id.sutoko_userstoryloader_filter),
    }
}