package com.example.sharedelements

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import dalvik.system.ZipPathValidator
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.Unzipper
import java.io.File
import java.util.Locale

object OnlineAssetsManager {

    enum class VersionComparision {
        LESSER,
        EQUAL,
        GREATER
    }

    fun hasStoryFiles(storyId: Int, version: String, symbols: TableOfSymbols): Boolean {
        return symbols.getStoryVersion(storyId) == version
    }

    fun hasUpdatableStoryFiles(storyId: Int, version: String, symbols: TableOfSymbols): Boolean {
        return symbols.getStoryVersion(storyId) != version && symbols.getStoryVersion(storyId) != "none"
                && symbols.getStoryVersion(storyId).isNotBlank()
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

    fun hasSomeStoryFiles(activity: Activity, storyId: Int): Boolean {
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
        storyId: Int,
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
        storyId: Int,
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

    fun removeDirectory(activity: Activity, storyId: Int) {
        val unzipLocation: String =
            File(activity.filesDir, "games/${storyId}").absolutePath
        if (File(unzipLocation).exists()) {
            File(unzipLocation).deleteRecursively()
        }
    }

    fun hasRequiredPermission(activity: Activity): Boolean {
        val f = activity.getExternalFilesDir(null)

        return f != null && f.canWrite()
    }

    fun requestRequiredPermission() {

    }

    fun getImageFilePath(context: Context, storyId: Int, assetName: String): String {
        val array = arrayOf("jpeg", "jpg", "png")
        array.forEach { extension ->

            if (File(
                    "${
                        SmsGameTreeStructure.getMediaFilePath(
                            context,
                            storyId,
                            assetName
                        )
                    }.$extension"
                ).exists()
            ) {
                return SmsGameTreeStructure.getMediaFilePath(
                    context,
                    storyId,
                    assetName
                ) + ".$extension"
            }
            if (File(
                    "${SmsGameTreeStructure.getMediaFilePath(context, storyId, assetName)}.${
                        extension.lowercase(
                            Locale.getDefault()
                        )
                    }"
                ).exists()
            ) {
                return SmsGameTreeStructure.getMediaFilePath(context, storyId, assetName) + ".${
                    extension.lowercase(
                        Locale.getDefault()
                    )
                }"
            }
        }

        return ""
    }

    fun getSoundFilePath(context: Context, storyId: Int, assetName: String): String {
        val array = arrayOf("mp3")
        val a = "sounds/$assetName"
        array.forEach { extension ->

            if (File(
                    "${
                        SmsGameTreeStructure.getMediaFilePath(
                            context,
                            storyId,
                            a
                        )
                    }.$extension"
                ).exists()
            ) {
                return SmsGameTreeStructure.getMediaFilePath(context, storyId, a) + ".$extension"
            }
            if (File(
                    "${SmsGameTreeStructure.getMediaFilePath(context, storyId, a)}.${
                        extension.uppercase()
                    }"
                ).exists()
            ) {
                return SmsGameTreeStructure.getMediaFilePath(context, storyId, a) + ".${
                    extension.lowercase(
                        Locale.getDefault()
                    )
                }"
            }
        }
        return ""
    }

    fun getJsonDirectory() {

    }
}