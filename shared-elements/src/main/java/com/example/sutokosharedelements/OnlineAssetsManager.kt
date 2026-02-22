package com.example.sutokosharedelements

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import dalvik.system.ZipPathValidator
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.Unzipper
import java.io.File

object OnlineAssetsManager {

    enum class VersionComparision {
        LESSER,
        EQUAL,
        GREATER
    }

    fun hasStoryFiles(storyId: String, version: String, symbols: TableOfSymbols): Boolean {
        return symbols.getStoryVersion(storyId.hashCode()) == version
    }

    fun hasUpdatableStoryFiles(storyId: String, version: String, symbols: TableOfSymbols): Boolean {
        return symbols.getStoryVersion(storyId.hashCode()) != version && symbols.getStoryVersion(storyId.hashCode()) != "none"
                && symbols.getStoryVersion(storyId.hashCode()).isNotBlank()
    }

    fun compareVersion(v1: String, v2: String): VersionComparision {
        if (v1.isBlank() || v2.isBlank()) {
            throw IllegalStateException("v1 : $v1, v2 : $v2")
        }

        val intV1 = v1.replace(".", "").toInt()
        val intV2 = v2.replace(".", "").toInt()

        if (intV1 == intV2) {
            return VersionComparision.EQUAL
        }
        if (intV1 < intV2) {
            return VersionComparision.LESSER
        }
        return VersionComparision.GREATER
    }

    fun hasSomeStoryFiles(activity: Activity, storyId: String): Boolean {
        return File(activity.getExternalFilesDir(null), "games/$storyId/").exists()
    }

    /**
     * Downloads the archive
     *
     * @param activity
     * @param storyId
     * @param storyVersion
     * @param onProgress
     * @param onSuccessDownload
     * @param onError
     */
    fun download(
        activity: Activity,
        storyId: String,
        storyVersion: String,
        onProgress: (p1: Long, p2: Long) -> Unit,
        onSuccessDownload: () -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {

        val storage: FirebaseStorage = FirebaseStorage.getInstance()

        val cardVersion: String = storyVersion.replace(".", "_")
        val archiveReference =
            storage.reference.child("stories/$storyId/archives/${cardVersion}.zip")
        val localFile = File.createTempFile("archive", "zip", activity.filesDir)

        archiveReference.getFile(localFile).addOnProgressListener {
            onProgress(it.bytesTransferred, it.totalByteCount)
        }.addOnSuccessListener {
            // Local temp file has been created
            val e = handleArchive(activity, storyId, storyVersion, localFile)
            if (e != "success") {
                localFile.delete()
                onError(e)
            } else {
                localFile.delete()
                onSuccessDownload()
            }
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(activity.applicationContext, "CODE B - ${it.message}", Toast.LENGTH_LONG)
                .show()
            onError(it.message ?: "")
        }
    }

    /**
     * Unzip the file, extracts it and suppress the archive
     *
     * @param tmpFile
     */
    private fun handleArchive(
        activity: Activity,
        storyId: String,
        version: String,
        tmpFile: File
    ): String {
        if (Build.VERSION.SDK_INT >= 34) {
            ZipPathValidator.clearCallback()
        }
        val zipFile: String =
            tmpFile.absolutePath

        val unzipLocation: String =
            File(activity.filesDir, "games/${storyId}").absolutePath
        removeDirectory(activity, storyId)

        val df = Unzipper(zipFile, unzipLocation)
        return df.unzip(activity as Context)
    }

    fun removeDirectory(activity: Activity, storyId: String) {
        val unzipLocation: String =
            File(activity.filesDir, "games/${storyId}").absolutePath
        if (File(unzipLocation).exists()) {
            File(unzipLocation).deleteRecursively()
        }
    }

    private val IMAGE_EXTENSIONS = listOf("jpeg", "jpg", "png")

    fun getImageFilePath(context: Context, storyId: String, assetName: String): String {
        val basePath = SmsGameTreeStructure.Companion.getMediaFilePath(context, storyId, assetName)
        
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
