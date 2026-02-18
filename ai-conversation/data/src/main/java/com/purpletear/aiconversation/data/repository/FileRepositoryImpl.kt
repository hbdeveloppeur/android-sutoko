package com.purpletear.aiconversation.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.purpletear.aiconversation.domain.repository.FileRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileRepositoryImpl(
    private val context: Context
) : FileRepository {
    override fun save(bitmap: Bitmap): Result<File> {
        val maxDimension = 780
        val scaledBitmap = scaleBitmap(bitmap, maxDimension)

        val tempFileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, tempFileName)

        try {
            FileOutputStream(tempFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            return Result.success(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Result.failure(IOException("Failed to save bitmap"))
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleFactor = when {
            width > height && width > maxDimension -> maxDimension.toFloat() / width
            height > maxDimension -> maxDimension.toFloat() / height
            else -> 1.0f
        }

        val scaledWidth = (width * scaleFactor).toInt()
        val scaledHeight = (height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
}