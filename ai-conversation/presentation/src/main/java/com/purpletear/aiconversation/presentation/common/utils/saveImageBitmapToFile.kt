package com.purpletear.aiconversation.presentation.common.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun saveImageBitmapToFile(
    context: Context,
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    quality: Int,
    fileName: String
): File {
    // Create a temporary file
    val file = File(context.cacheDir, fileName)

    FileOutputStream(file).use { out ->
        bitmap.compress(format, quality, out)
    }
    return file
}