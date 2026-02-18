package com.purpletear.core.image_downloader

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException


class ImageDownloaderImpl(private val context: Context) : ImageDownloader {

    override fun download(url: String): Flow<Result<Unit>> = flow {
        try {
            val bitmap = loadBitmapFromUrl(url)
            saveImageToGallery(bitmap, generateUniqueFilename("jpg"))
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun loadBitmapFromUrl(url: String): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val drawable = (context.imageLoader.execute(request).drawable as? BitmapDrawable)
            ?: throw IllegalStateException("Unable to download image")
        return drawable.bitmap
    }

    @SuppressLint("InlinedApi")
    private fun saveImageToGallery(bitmap: Bitmap, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/MyAppGallery"
            ) // Save in Pictures/MyAppGallery
        }

        val uri: Uri? = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    throw IOException("Failed to save bitmap.")
                }
            } ?: throw IOException("Failed to get output stream.")
        } ?: throw IOException("Failed to create new MediaStore record.")
    }

    private fun generateUniqueFilename(extension: String): String {
        return "${System.currentTimeMillis()}.$extension"
    }
}