package com.example.sharedelements

import android.content.Context
import java.io.File

object OnlineAssetsManager {

    private val IMAGE_EXTENSIONS = listOf("jpeg", "jpg", "png")

    fun getImageFilePath(context: Context, storyId: String, assetName: String): String {
        val basePath = SmsGameTreeStructure.getMediaFilePath(context, storyId, assetName)

        File(basePath).takeIf { it.exists() }?.let { return basePath }

        return IMAGE_EXTENSIONS.firstNotNullOfOrNull { extension ->
            "$basePath.$extension".takeIf { File(it).exists() }
        } ?: ""
    }

    private val SOUND_EXTENSIONS = listOf("mp3", "ogg", "wav")
    private const val SOUNDS_SUBDIR = "sounds"

    fun getSoundFilePath(context: Context, storyId: String, assetName: String): String {
        val basePath = SmsGameTreeStructure.getMediaFilePath(
            context,
            storyId,
            "$SOUNDS_SUBDIR/$assetName"
        )

        return SOUND_EXTENSIONS.firstNotNullOfOrNull { extension ->
            listOf(extension.lowercase(), extension.uppercase())
                .firstOrNull { variant -> File("$basePath.$variant").exists() }
                ?.let { "$basePath.$it" }
        } ?: ""
    }
}
